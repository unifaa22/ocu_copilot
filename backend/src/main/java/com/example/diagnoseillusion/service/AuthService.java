package com.example.diagnoseillusion.service;

import com.example.diagnoseillusion.common.CustomException;
import com.example.diagnoseillusion.dto.request.LoginRequest;
import com.example.diagnoseillusion.dto.request.RegisterRequest;
import com.example.diagnoseillusion.dto.response.AuthResponse;
import com.example.diagnoseillusion.dto.response.UserResponse;
import com.example.diagnoseillusion.entity.SysUser;
import com.example.diagnoseillusion.enums.DeletedFlag;
import com.example.diagnoseillusion.repository.SysUserRepository;
import com.example.diagnoseillusion.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final SysUserRepository sysUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final UserService userService;
    private final UserRoleHelper userRoleHelper;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new CustomException(400, "两次密码不一致");
        }
        if (sysUserRepository.existsByUsernameAndIsDeleted(request.getUsername(), DeletedFlag.NOT_DELETED)) {
            throw new CustomException(409, "用户名已存在");
        }
        SysUser user = new SysUser();
        user.setUsername(request.getUsername().trim());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setIsDeleted(DeletedFlag.NOT_DELETED);
        sysUserRepository.save(user);
        userRoleHelper.assignDefaultUserRole(user);
        return buildAuthResponse(user);
    }

    public AuthResponse login(LoginRequest request) {
        SysUser user = sysUserRepository.findByUsernameAndIsDeleted(request.getUsername(), DeletedFlag.NOT_DELETED)
                .orElseThrow(() -> new CustomException(400, "用户名或密码错误"));
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new CustomException(400, "用户名或密码错误");
        }
        return buildAuthResponse(user);
    }

    private AuthResponse buildAuthResponse(SysUser user) {
        UserResponse userResponse = userService.toUserResponse(user, false);
        String token = jwtService.generateToken(user.getId(), user.getUsername(), userResponse.getRoles());
        return new AuthResponse(token, userResponse);
    }
}
