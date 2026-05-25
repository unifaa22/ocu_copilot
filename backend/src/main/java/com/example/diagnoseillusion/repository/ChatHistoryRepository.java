package com.example.diagnoseillusion.repository;

import com.example.diagnoseillusion.entity.ChatHistory;
import com.example.diagnoseillusion.enums.DeletedFlag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ChatHistoryRepository extends JpaRepository<ChatHistory, Long> {

    List<ChatHistory> findByUser_IdAndIsDeletedOrderByCreateTimeDesc(Long userId, DeletedFlag isDeleted);

    @Query("SELECT h FROM ChatHistory h WHERE h.user.id = :userId AND h.isDeleted = :deleted " +
            "AND (:personalOnly = false OR h.team IS NULL) " +
            "AND (:teamOnly = false OR h.team IS NOT NULL)")
    List<ChatHistory> findActiveForUser(@Param("userId") Long userId,
                                        @Param("deleted") DeletedFlag deleted,
                                        @Param("personalOnly") boolean personalOnly,
                                        @Param("teamOnly") boolean teamOnly);

    List<ChatHistory> findByConversationIdAndUser_IdAndIsDeletedOrderByCreateTimeAsc(
            String conversationId, Long userId, DeletedFlag isDeleted);

    Optional<ChatHistory> findByIdAndUser_Id(Long id, Long userId);

    @Modifying
    @Query("UPDATE ChatHistory h SET h.isDeleted = :deleted WHERE h.conversationId = :conversationId AND h.user.id = :userId")
    int softDeleteByConversation(@Param("conversationId") String conversationId,
                                 @Param("userId") Long userId,
                                 @Param("deleted") DeletedFlag deleted);

    @Modifying
    @Query("UPDATE ChatHistory h SET h.isDeleted = :deleted WHERE h.user.id = :userId AND h.isDeleted = :notDeleted")
    int softDeleteAllByUser(@Param("userId") Long userId,
                            @Param("deleted") DeletedFlag deleted,
                            @Param("notDeleted") DeletedFlag notDeleted);

    Page<ChatHistory> findByUser_IdAndIsDeleted(Long userId, DeletedFlag isDeleted, Pageable pageable);
}
