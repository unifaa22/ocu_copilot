package com.example.diagnoseillusion.service;

import com.example.diagnoseillusion.config.DataInitializer;
import com.example.diagnoseillusion.entity.Role;
import com.example.diagnoseillusion.entity.SysUser;
import com.example.diagnoseillusion.entity.UserRole;
import com.example.diagnoseillusion.enums.DeletedFlag;
import com.example.diagnoseillusion.repository.RoleRepository;
import com.example.diagnoseillusion.repository.UserRoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class UserRoleHelper {

    private final UserRoleRepository userRoleRepository;
    private final RoleRepository roleRepository;

    public List<String> getRoleCodes(Long userId) {
        return userRoleRepository.findActiveByUserId(userId, DeletedFlag.NOT_DELETED).stream()
                .map(ur -> ur.getRole().getRoleName())
                .toList();
    }

    public void assignRoleIfAbsent(SysUser user, String roleName) {
        if (userRoleRepository.existsByUser_IdAndRole_RoleNameAndIsDeleted(user.getId(), roleName, DeletedFlag.NOT_DELETED)) {
            return;
        }
        Role role = roleRepository.findByRoleNameAndIsDeleted(roleName, DeletedFlag.NOT_DELETED)
                .orElseThrow(() -> new IllegalStateException("Role not found: " + roleName));
        UserRole userRole = new UserRole();
        userRole.setUser(user);
        userRole.setRole(role);
        userRole.setIsDeleted(DeletedFlag.NOT_DELETED);
        userRoleRepository.save(userRole);
    }

    public void assignDefaultUserRole(SysUser user) {
        assignRoleIfAbsent(user, DataInitializer.ROLE_USER);
    }

    public void assignTeamCreatorRole(SysUser user) {
        assignRoleIfAbsent(user, DataInitializer.ROLE_TEAM_CREATOR);
    }
}
