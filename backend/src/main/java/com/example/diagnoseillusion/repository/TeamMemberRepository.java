package com.example.diagnoseillusion.repository;

import com.example.diagnoseillusion.entity.TeamMember;
import com.example.diagnoseillusion.enums.DeletedFlag;
import com.example.diagnoseillusion.enums.TeamMemberStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TeamMemberRepository extends JpaRepository<TeamMember, Long> {

    Optional<TeamMember> findByTeam_IdAndUser_Id(Long teamId, Long userId);

    Optional<TeamMember> findByTeam_IdAndUser_IdAndIsDeleted(Long teamId, Long userId, DeletedFlag isDeleted);

    @Query("SELECT m FROM TeamMember m JOIN FETCH m.team t JOIN FETCH t.creator " +
            "WHERE m.user.id = :userId AND m.status = :status AND m.isDeleted = :deleted AND t.isDeleted = :deleted")
    Page<TeamMember> findJoinedTeams(@Param("userId") Long userId,
                                     @Param("status") TeamMemberStatus status,
                                     @Param("deleted") DeletedFlag deleted,
                                     Pageable pageable);

    @Query("SELECT m FROM TeamMember m JOIN FETCH m.team t JOIN FETCH t.creator " +
            "WHERE m.user.id = :userId AND m.status = :status AND m.isDeleted = :deleted AND t.isDeleted = :deleted")
    Page<TeamMember> findPendingInvitations(@Param("userId") Long userId,
                                            @Param("status") TeamMemberStatus status,
                                            @Param("deleted") DeletedFlag deleted,
                                            Pageable pageable);

    List<TeamMember> findByTeam_IdAndStatusAndIsDeleted(Long teamId, TeamMemberStatus status, DeletedFlag isDeleted);

    long countByTeam_IdAndStatusAndIsDeleted(Long teamId, TeamMemberStatus status, DeletedFlag isDeleted);

    @Query("SELECT CASE WHEN COUNT(m) > 0 THEN true ELSE false END FROM TeamMember m " +
            "JOIN m.team t WHERE m.user.id = :userId AND m.status = :status AND m.isDeleted = :deleted " +
            "AND t.isDeleted = :deleted AND t.isShare = com.example.diagnoseillusion.enums.ShareStatus.ENABLED " +
            "AND t.creator.id = :creatorId")
    boolean hasSharedAccessToCreatorFiles(@Param("userId") Long userId,
                                          @Param("creatorId") Long creatorId,
                                          @Param("status") TeamMemberStatus status,
                                          @Param("deleted") DeletedFlag deleted);

    List<TeamMember> findByTeam_IdAndIsDeleted(Long teamId, DeletedFlag isDeleted);
}
