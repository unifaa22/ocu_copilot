package com.example.diagnoseillusion.repository;

import com.example.diagnoseillusion.entity.Note;
import com.example.diagnoseillusion.enums.DeletedFlag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface NoteRepository extends JpaRepository<Note, Long> {

    Optional<Note> findByIdAndUser_IdAndIsDeleted(Long id, Long userId, DeletedFlag isDeleted);

    @Query("SELECT n FROM Note n WHERE n.user.id = :userId AND n.isDeleted = :deleted " +
            "AND (:keyword IS NULL OR LOWER(n.title) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Note> searchByUser(@Param("userId") Long userId,
                            @Param("deleted") DeletedFlag deleted,
                            @Param("keyword") String keyword,
                            Pageable pageable);
}
