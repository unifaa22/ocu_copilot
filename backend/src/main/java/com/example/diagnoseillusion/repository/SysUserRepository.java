package com.example.diagnoseillusion.repository;

import com.example.diagnoseillusion.entity.SysUser;
import com.example.diagnoseillusion.enums.DeletedFlag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SysUserRepository extends JpaRepository<SysUser, Long> {

    Optional<SysUser> findByUsernameAndIsDeleted(String username, DeletedFlag isDeleted);

    boolean existsByUsernameAndIsDeleted(String username, DeletedFlag isDeleted);
}
