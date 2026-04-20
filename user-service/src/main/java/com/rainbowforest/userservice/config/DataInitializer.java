package com.rainbowforest.userservice.config;

import com.rainbowforest.userservice.entity.UserRole;
import com.rainbowforest.userservice.repository.UserRoleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    private final UserRoleRepository userRoleRepository;

    public DataInitializer(UserRoleRepository userRoleRepository) {
        this.userRoleRepository = userRoleRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        ensureRole("ROLE_ADMIN");
        ensureRole("ROLE_USER");
    }

    private void ensureRole(String roleName) {
        UserRole r = userRoleRepository.findUserRoleByRoleName(roleName);
        if (r == null) {
            r = new UserRole();
            r.setRoleName(roleName);
            userRoleRepository.save(r);
            logger.info("Created missing role: {}", roleName);
        }
    }
}
