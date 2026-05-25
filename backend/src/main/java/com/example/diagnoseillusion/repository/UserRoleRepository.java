package com.example.diagnoseillusion.repository;

import com.example.diagnoseillusion.entity.UserRole;
import com.example.diagnoseillusion.enums.DeletedFlag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserRoleRepository extends JpaRepository<UserRole, Long> {

    @Query("SELECT ur FROM UserRole ur JOIN FETCH ur.role r WHERE ur.user.id = :userId AND ur.isDeleted = :deleted AND r.isDeleted = :deleted")
    List<UserRole> findActiveByUserId(@Param("userId") Long userId, @Param("deleted") DeletedFlag deleted);

    boolean existsByUser_IdAndRole_RoleNameAndIsDeleted(Long userId, String roleName, DeletedFlag deleted);
}
