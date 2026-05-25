package com.example.diagnoseillusion.repository;

import com.example.diagnoseillusion.entity.Team;
import com.example.diagnoseillusion.enums.DeletedFlag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TeamRepository extends JpaRepository<Team, Long> {

    Optional<Team> findByIdAndIsDeleted(Long id, DeletedFlag isDeleted);

    Page<Team> findByCreator_IdAndIsDeleted(Long creatorId, DeletedFlag isDeleted, Pageable pageable);
}
