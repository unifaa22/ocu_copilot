package com.example.diagnoseillusion.config;

import com.example.diagnoseillusion.entity.Role;
import com.example.diagnoseillusion.enums.DeletedFlag;
import com.example.diagnoseillusion.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    public static final String ROLE_USER = "USER";
    public static final String ROLE_TEAM_CREATOR = "TEAM_CREATOR";

    private final RoleRepository roleRepository;

    @Override
    public void run(String... args) {
        ensureRole(ROLE_USER);
        ensureRole(ROLE_TEAM_CREATOR);
    }

    private void ensureRole(String roleName) {
        if (roleRepository.findByRoleNameAndIsDeleted(roleName, DeletedFlag.NOT_DELETED).isEmpty()) {
            Role role = new Role();
            role.setRoleName(roleName);
            role.setIsDeleted(DeletedFlag.NOT_DELETED);
            roleRepository.save(role);
        }
    }
}
