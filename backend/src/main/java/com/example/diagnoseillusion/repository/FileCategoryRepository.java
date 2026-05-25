package com.example.diagnoseillusion.repository;

import com.example.diagnoseillusion.entity.FileCategory;
import com.example.diagnoseillusion.enums.DeletedFlag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FileCategoryRepository extends JpaRepository<FileCategory, Long> {

    List<FileCategory> findByUser_IdAndIsDeletedOrderByCreateTimeDesc(Long userId, DeletedFlag isDeleted);

    Optional<FileCategory> findByIdAndUser_IdAndIsDeleted(Long id, Long userId, DeletedFlag isDeleted);

    Optional<FileCategory> findByIdAndIsDeleted(Long id, DeletedFlag isDeleted);

    boolean existsByUser_IdAndCategoryNameAndIsDeleted(Long userId, String categoryName, DeletedFlag isDeleted);

    boolean existsByUser_IdAndCategoryNameAndIsDeletedAndIdNot(Long userId, String categoryName, DeletedFlag isDeleted, Long id);

    List<FileCategory> findByUser_IdAndIsDeleted(Long creatorId, DeletedFlag isDeleted);
}
