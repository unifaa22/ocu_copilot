package com.example.diagnoseillusion.service;

import com.example.diagnoseillusion.common.CustomException;
import com.example.diagnoseillusion.config.AppProperties;
import com.example.diagnoseillusion.dto.request.ChangePasswordRequest;
import com.example.diagnoseillusion.dto.response.AvatarResponse;
import com.example.diagnoseillusion.dto.response.UserResponse;
import com.example.diagnoseillusion.entity.SysUser;
import com.example.diagnoseillusion.enums.DeletedFlag;
import com.example.diagnoseillusion.repository.SysUserRepository;
import com.example.diagnoseillusion.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private static final Set<String> AVATAR_TYPES = Set.of("image/jpeg", "image/jpg", "image/png");

    private final SysUserRepository sysUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final MinioStorageService minioStorageService;
    private final UserRoleHelper userRoleHelper;
    private final AppProperties appProperties;

    public UserResponse getProfile() {
        SysUser user = requireCurrentUser();
        return toUserResponse(user, true);
    }

    @Transactional
    public void changePassword(ChangePasswordRequest request) {
        SysUser user = requireCurrentUser();
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new CustomException(400, "两次新密码不一致");
        }
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new CustomException(400, "旧密码错误");
        }
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        sysUserRepository.save(user);
    }

    @Transactional
    public AvatarResponse uploadAvatar(MultipartFile file) {
        SysUser user = requireCurrentUser();
        if (file == null || file.isEmpty()) {
            throw new CustomException(400, "请选择图片");
        }
        long maxBytes = appProperties.getUpload().getAvatarMaxSizeMb() * 1024L * 1024L;
        if (file.getSize() > maxBytes) {
            throw new CustomException(400, "头像超过大小上限");
        }
        String contentType = file.getContentType();
        if (contentType == null || !AVATAR_TYPES.contains(contentType.toLowerCase())) {
            throw new CustomException(400, "仅支持 jpg/png 图片");
        }
        String ext = contentType.contains("png") ? "png" : "jpg";
        String objectKey = "avatars/" + user.getId() + "/" + UUID.randomUUID() + "." + ext;
        if (user.getAvatar() != null) {
            minioStorageService.delete(user.getAvatar());
        }
        minioStorageService.upload(objectKey, file, contentType);
        user.setAvatar(objectKey);
        sysUserRepository.save(user);
        return new AvatarResponse(objectKey, presignedAvatar(user));
    }

    public UserResponse toUserResponse(SysUser user, boolean withAvatarUrl) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setUsername(user.getUsername());
        response.setAvatar(user.getAvatar());
        response.setAvatarUrl(withAvatarUrl ? presignedAvatar(user) : null);
        response.setRoles(userRoleHelper.getRoleCodes(user.getId()));
        return response;
    }

    public SysUser requireCurrentUser() {
        return sysUserRepository.findById(SecurityUtils.getCurrentUserId())
                .filter(u -> u.getIsDeleted() == DeletedFlag.NOT_DELETED)
                .orElseThrow(() -> new CustomException(401, "未认证，请先登录"));
    }

    public SysUser findActiveById(Long id) {
        return sysUserRepository.findById(id)
                .filter(u -> u.getIsDeleted() == DeletedFlag.NOT_DELETED)
                .orElseThrow(() -> new CustomException(404, "用户不存在"));
    }

    public SysUser findActiveByUsername(String username) {
        return sysUserRepository.findByUsernameAndIsDeleted(username, DeletedFlag.NOT_DELETED)
                .orElseThrow(() -> new CustomException(404, "用户不存在"));
    }

    private String presignedAvatar(SysUser user) {
        if (user.getAvatar() == null || user.getAvatar().isBlank()) {
            return null;
        }
        return minioStorageService.presignedGetUrl(user.getAvatar(), 3600);
    }
}
