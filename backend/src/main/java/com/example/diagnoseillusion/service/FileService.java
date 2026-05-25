package com.example.diagnoseillusion.service;

import com.example.diagnoseillusion.common.CustomException;
import com.example.diagnoseillusion.common.PageUtils;
import com.example.diagnoseillusion.config.AppProperties;
import com.example.diagnoseillusion.dto.common.PageResult;
import com.example.diagnoseillusion.dto.request.FileRenameRequest;
import com.example.diagnoseillusion.dto.response.FileResponse;
import com.example.diagnoseillusion.dto.response.PreviewUrlResponse;
import com.example.diagnoseillusion.entity.FileCategory;
import com.example.diagnoseillusion.entity.KnowledgeFile;
import com.example.diagnoseillusion.enums.DeletedFlag;
import com.example.diagnoseillusion.enums.SyncStatus;
import com.example.diagnoseillusion.enums.TeamMemberStatus;
import com.example.diagnoseillusion.repository.KnowledgeFileRepository;
import com.example.diagnoseillusion.repository.TeamMemberRepository;
import com.example.diagnoseillusion.security.SecurityUtils;
import com.example.diagnoseillusion.service.dify.DifyClient;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FileService {

    private static final Set<String> ALLOWED_EXT = Set.of("md", "pdf", "doc", "docx");
    private static final int PREVIEW_EXPIRE_SECONDS = 3600;

    private final KnowledgeFileRepository knowledgeFileRepository;
    private final CategoryService categoryService;
    private final UserService userService;
    private final MinioStorageService minioStorageService;
    private final DifyClient difyClient;
    private final TeamMemberRepository teamMemberRepository;
    private final AppProperties appProperties;

    public PageResult<FileResponse> listByCategory(Long categoryId, Integer page, Integer size, Byte syncStatus) {
        categoryService.requireOwnedCategory(categoryId);
        Pageable pageable = PageUtils.pageable(page, size);
        SyncStatus status = parseSyncStatus(syncStatus);
        Page<KnowledgeFile> result = knowledgeFileRepository.findByCategoryPaged(
                categoryId, DeletedFlag.NOT_DELETED, status, pageable);
        return PageUtils.toPageResult(result.map(this::toResponse));
    }

    public PageResult<FileResponse> listAll(Integer page, Integer size, Byte syncStatus) {
        Long userId = SecurityUtils.getCurrentUserId();
        Pageable pageable = PageUtils.pageable(page, size);
        SyncStatus status = parseSyncStatus(syncStatus);
        Page<KnowledgeFile> result = knowledgeFileRepository.findByUserPaged(userId, DeletedFlag.NOT_DELETED, status, pageable);
        return PageUtils.toPageResult(result.map(this::toResponse));
    }

    @Transactional
    public FileResponse upload(Long categoryId, MultipartFile file) {
        FileCategory category = categoryService.requireOwnedCategory(categoryId);
        validateLearningFile(file);
        String ext = getExtension(file.getOriginalFilename());
        Long userId = SecurityUtils.getCurrentUserId();
        String objectKey = "files/" + userId + "/" + categoryId + "/" + UUID.randomUUID() + "." + ext;
        minioStorageService.upload(objectKey, file, file.getContentType());

        KnowledgeFile knowledgeFile = new KnowledgeFile();
        knowledgeFile.setFileName(file.getOriginalFilename());
        knowledgeFile.setFileType(ext);
        knowledgeFile.setFilePath(objectKey);
        knowledgeFile.setFileSize(file.getSize());
        knowledgeFile.setCategory(category);
        knowledgeFile.setUser(userService.requireCurrentUser());
        knowledgeFile.setSyncStatus(SyncStatus.UNSYNCED);
        knowledgeFile.setIsDeleted(DeletedFlag.NOT_DELETED);
        return toResponse(knowledgeFileRepository.save(knowledgeFile));
    }

    @Transactional
    public FileResponse rename(Long id, FileRenameRequest request) {
        KnowledgeFile file = requireOwnedFile(id);
        file.setFileName(request.getFileName());
        return toResponse(knowledgeFileRepository.save(file));
    }

    @Transactional
    public void delete(Long id) {
        KnowledgeFile file = requireOwnedFile(id);
        file.setIsDeleted(DeletedFlag.DELETED);
        knowledgeFileRepository.save(file);
        minioStorageService.delete(file.getFilePath());
        FileCategory category = file.getCategory();
        if (file.getDifyDocumentId() != null && category.getDifyDatasetId() != null) {
            try {
                difyClient.deleteDocument(category.getDifyDatasetId(), file.getDifyDocumentId());
            } catch (Exception ignored) {
            }
        }
    }

    public PreviewUrlResponse previewUrl(Long fileId) {
        KnowledgeFile file = knowledgeFileRepository.findByIdAndIsDeleted(fileId, DeletedFlag.NOT_DELETED)
                .orElseThrow(() -> new CustomException(404, "文件不存在"));
        Long userId = SecurityUtils.getCurrentUserId();
        if (!file.getUser().getId().equals(userId)) {
            boolean canAccess = teamMemberRepository.hasSharedAccessToCreatorFiles(
                    userId, file.getUser().getId(), TeamMemberStatus.JOINED, DeletedFlag.NOT_DELETED);
            if (!canAccess) {
                throw new CustomException(403, "无权限访问");
            }
        }
        String url = minioStorageService.presignedGetUrl(file.getFilePath(), PREVIEW_EXPIRE_SECONDS);
        return new PreviewUrlResponse(url, PREVIEW_EXPIRE_SECONDS);
    }

    public PageResult<FileResponse> listSharedFiles(Long categoryId, Integer page, Integer size) {
        Pageable pageable = PageUtils.pageable(page, size);
        Page<KnowledgeFile> result = knowledgeFileRepository.findByCategoryPaged(
                categoryId, DeletedFlag.NOT_DELETED, null, pageable);
        return PageUtils.toPageResult(result.map(this::toResponse));
    }

    public KnowledgeFile requireOwnedFile(Long id) {
        return knowledgeFileRepository.findByIdAndUser_IdAndIsDeleted(id, SecurityUtils.getCurrentUserId(), DeletedFlag.NOT_DELETED)
                .orElseThrow(() -> new CustomException(404, "文件不存在"));
    }

    public FileResponse toResponse(KnowledgeFile file) {
        FileResponse response = new FileResponse();
        response.setId(file.getId());
        response.setFileName(file.getFileName());
        response.setFileType(file.getFileType());
        response.setFileSize(file.getFileSize());
        response.setCategoryId(file.getCategory().getId());
        response.setSyncStatus(file.getSyncStatus().getValue());
        response.setCreateTime(file.getCreateTime());
        response.setUpdateTime(file.getUpdateTime());
        return response;
    }

    private void validateLearningFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new CustomException(400, "文件不能为空");
        }
        String ext = getExtension(file.getOriginalFilename());
        if (!ALLOWED_EXT.contains(ext)) {
            throw new CustomException(400, "文件类型不支持");
        }
        long maxBytes = appProperties.getDify().getUpload().getMaxFileSizeMb() * 1024L * 1024L;
        if (file.getSize() > maxBytes) {
            throw new CustomException(400, "文件超过大小上限");
        }
    }

    private SyncStatus parseSyncStatus(Byte syncStatus) {
        if (syncStatus == null) {
            return null;
        }
        for (SyncStatus value : SyncStatus.values()) {
            if (value.getValue() == syncStatus) {
                return value;
            }
        }
        return null;
    }

    private String getExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
    }
}
