package com.example.diagnoseillusion.repository;

import com.example.diagnoseillusion.entity.KnowledgeFile;
import com.example.diagnoseillusion.enums.DeletedFlag;
import com.example.diagnoseillusion.enums.SyncStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface KnowledgeFileRepository extends JpaRepository<KnowledgeFile, Long> {

    Optional<KnowledgeFile> findByIdAndUser_IdAndIsDeleted(Long id, Long userId, DeletedFlag isDeleted);

    Optional<KnowledgeFile> findByIdAndIsDeleted(Long id, DeletedFlag isDeleted);

    long countByCategory_IdAndIsDeleted(Long categoryId, DeletedFlag isDeleted);

    long countByCategory_IdAndSyncStatusAndIsDeleted(Long categoryId, SyncStatus syncStatus, DeletedFlag isDeleted);

    List<KnowledgeFile> findByCategory_IdAndIsDeletedAndSyncStatusIn(
            Long categoryId, DeletedFlag isDeleted, List<SyncStatus> syncStatuses);

    @Query("SELECT f FROM KnowledgeFile f WHERE f.category.id = :categoryId AND f.isDeleted = :deleted " +
            "AND (:syncStatus IS NULL OR f.syncStatus = :syncStatus)")
    Page<KnowledgeFile> findByCategoryPaged(@Param("categoryId") Long categoryId,
                                            @Param("deleted") DeletedFlag deleted,
                                            @Param("syncStatus") SyncStatus syncStatus,
                                            Pageable pageable);

    @Query("SELECT f FROM KnowledgeFile f WHERE f.user.id = :userId AND f.isDeleted = :deleted " +
            "AND (:syncStatus IS NULL OR f.syncStatus = :syncStatus)")
    Page<KnowledgeFile> findByUserPaged(@Param("userId") Long userId,
                                        @Param("deleted") DeletedFlag deleted,
                                        @Param("syncStatus") SyncStatus syncStatus,
                                        Pageable pageable);

    List<KnowledgeFile> findByCategory_IdAndIsDeleted(Long categoryId, DeletedFlag isDeleted);

    boolean existsByCategory_IdAndSyncStatusAndIsDeleted(Long categoryId, SyncStatus syncStatus, DeletedFlag isDeleted);
}
