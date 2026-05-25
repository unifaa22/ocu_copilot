package com.example.diagnoseillusion.service;

import com.example.diagnoseillusion.common.CustomException;
import com.example.diagnoseillusion.dto.request.CategoryNameRequest;
import com.example.diagnoseillusion.dto.response.CategoryResponse;
import com.example.diagnoseillusion.dto.response.SyncResultResponse;
import com.example.diagnoseillusion.entity.FileCategory;
import com.example.diagnoseillusion.entity.KnowledgeFile;
import com.example.diagnoseillusion.entity.SysUser;
import com.example.diagnoseillusion.enums.DeletedFlag;
import com.example.diagnoseillusion.enums.SyncStatus;
import com.example.diagnoseillusion.repository.FileCategoryRepository;
import com.example.diagnoseillusion.repository.KnowledgeFileRepository;
import com.example.diagnoseillusion.security.SecurityUtils;
import com.example.diagnoseillusion.service.dify.DifyClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final FileCategoryRepository fileCategoryRepository;
    private final KnowledgeFileRepository knowledgeFileRepository;
    private final UserService userService;
    private final MinioStorageService minioStorageService;
    private final DifyClient difyClient;

    public List<CategoryResponse> listMyCategories() {
        Long userId = SecurityUtils.getCurrentUserId();
        return fileCategoryRepository.findByUser_IdAndIsDeletedOrderByCreateTimeDesc(userId, DeletedFlag.NOT_DELETED)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public CategoryResponse create(CategoryNameRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        String name = request.getCategoryName().trim();
        if (fileCategoryRepository.existsByUser_IdAndCategoryNameAndIsDeleted(userId, name, DeletedFlag.NOT_DELETED)) {
            throw new CustomException(409, "分类名已存在");
        }
        SysUser user = userService.requireCurrentUser();
        FileCategory category = new FileCategory();
        category.setCategoryName(name);
        category.setUser(user);
        category.setIsDeleted(DeletedFlag.NOT_DELETED);
        return toResponse(fileCategoryRepository.save(category));
    }

    @Transactional
    public CategoryResponse rename(Long id, CategoryNameRequest request) {
        FileCategory category = requireOwnedCategory(id);
        String name = request.getCategoryName().trim();
        Long userId = SecurityUtils.getCurrentUserId();
        if (fileCategoryRepository.existsByUser_IdAndCategoryNameAndIsDeletedAndIdNot(
                userId, name, DeletedFlag.NOT_DELETED, id)) {
            throw new CustomException(409, "分类名已存在");
        }
        category.setCategoryName(name);
        return toResponse(fileCategoryRepository.save(category));
    }

    @Transactional
    public void delete(Long id) {
        FileCategory category = requireOwnedCategory(id);
        category.setIsDeleted(DeletedFlag.DELETED);
        category.setCategoryName(category.getCategoryName() + "__del_" + category.getId());
        fileCategoryRepository.save(category);

        List<KnowledgeFile> files = knowledgeFileRepository.findByCategory_IdAndIsDeleted(id, DeletedFlag.NOT_DELETED);
        for (KnowledgeFile file : files) {
            file.setIsDeleted(DeletedFlag.DELETED);
            knowledgeFileRepository.save(file);
            minioStorageService.delete(file.getFilePath());
            if (file.getDifyDocumentId() != null && category.getDifyDatasetId() != null) {
                try {
                    difyClient.deleteDocument(category.getDifyDatasetId(), file.getDifyDocumentId());
                } catch (Exception ignored) {
                }
            }
        }
        if (category.getDifyDatasetId() != null) {
            try {
                difyClient.deleteDataset(category.getDifyDatasetId());
            } catch (Exception ignored) {
            }
        }
    }

    @Transactional
    public SyncResultResponse sync(Long categoryId) {
        FileCategory category = requireOwnedCategory(categoryId);
        if (category.getDifyDatasetId() == null || category.getDifyDatasetId().isBlank()) {
            category.setDifyDatasetId(difyClient.createDataset(category.getCategoryName()));
            fileCategoryRepository.save(category);
        }
        List<KnowledgeFile> pending = knowledgeFileRepository.findByCategory_IdAndIsDeletedAndSyncStatusIn(
                categoryId, DeletedFlag.NOT_DELETED, List.of(SyncStatus.UNSYNCED, SyncStatus.FAILED));

        List<SyncResultResponse.FailedFileItem> failedFiles = new ArrayList<>();
        int success = 0;
        for (KnowledgeFile file : pending) {
            try {
                byte[] bytes = minioStorageService.readObjectBytes(file.getFilePath());
                String docId = difyClient.uploadDocument(
                        category.getDifyDatasetId(),
                        file.getFileName(),
                        new java.io.ByteArrayInputStream(bytes),
                        file.getFileSize() != null ? file.getFileSize() : bytes.length,
                        contentTypeFor(file.getFileType()));
                file.setDifyDocumentId(docId);
                file.setSyncStatus(SyncStatus.SUCCESS);
                knowledgeFileRepository.save(file);
                success++;
            } catch (Exception e) {
                file.setSyncStatus(SyncStatus.FAILED);
                knowledgeFileRepository.save(file);
                SyncResultResponse.FailedFileItem item = new SyncResultResponse.FailedFileItem();
                item.setFileId(file.getId());
                item.setFileName(file.getFileName());
                item.setSyncStatus(SyncStatus.FAILED.getValue());
                item.setErrorMessage(e.getMessage() != null ? e.getMessage() : "同步失败");
                failedFiles.add(item);
            }
        }
        SyncResultResponse result = new SyncResultResponse();
        result.setCategoryId(categoryId);
        result.setTotal(pending.size());
        result.setSuccessCount(success);
        result.setFailCount(failedFiles.size());
        result.setFailedFiles(failedFiles);
        return result;
    }

    public List<CategoryResponse> listByCreatorId(Long creatorId) {
        return fileCategoryRepository.findByUser_IdAndIsDeleted(creatorId, DeletedFlag.NOT_DELETED)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public FileCategory requireOwnedCategory(Long id) {
        return fileCategoryRepository.findByIdAndUser_IdAndIsDeleted(id, SecurityUtils.getCurrentUserId(), DeletedFlag.NOT_DELETED)
                .orElseThrow(() -> new CustomException(404, "分类不存在"));
    }

    public CategoryResponse toResponse(FileCategory category) {
        CategoryResponse response = new CategoryResponse();
        response.setId(category.getId());
        response.setCategoryName(category.getCategoryName());
        response.setFileCount(knowledgeFileRepository.countByCategory_IdAndIsDeleted(category.getId(), DeletedFlag.NOT_DELETED));
        response.setSyncedCount(knowledgeFileRepository.countByCategory_IdAndSyncStatusAndIsDeleted(
                category.getId(), SyncStatus.SUCCESS, DeletedFlag.NOT_DELETED));
        response.setCreateTime(category.getCreateTime());
        response.setUpdateTime(category.getUpdateTime());
        return response;
    }

    private String contentTypeFor(String fileType) {
        return switch (fileType) {
            case "pdf" -> "application/pdf";
            case "doc" -> "application/msword";
            case "docx" -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
            default -> "text/plain";
        };
    }
}
