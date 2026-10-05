package com.biharlearning.config;

import com.biharlearning.entity.AppUser;
import com.biharlearning.repository.AppUserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Creates a bootstrap admin user on first run from env/config. For local/dev convenience;
 * override ADMIN_EMAIL / ADMIN_PASSWORD in production and rotate the secret.
 */
@Component
public class AdminBootstrap implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminBootstrap.class);

    private final AppUserRepository userRepo;
    private final PasswordEncoder encoder;
    private final String adminEmail;
    private final String adminPassword;

    public AdminBootstrap(AppUserRepository userRepo, PasswordEncoder encoder,
                          @Value("${app.security.admin.email}") String adminEmail,
                          @Value("${app.security.admin.password}") String adminPassword) {
        this.userRepo = userRepo;
        this.encoder = encoder;
        this.adminEmail = adminEmail;
        this.adminPassword = adminPassword;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (userRepo.findByEmail(adminEmail).isEmpty()) {
            AppUser admin = new AppUser();
            admin.setEmail(adminEmail);
            admin.setDisplayName("Administrator");
            admin.setRole("ADMIN");
            admin.setPasswordHash(encoder.encode(adminPassword));
            userRepo.save(admin);
            log.info("Bootstrap admin created: {} (change the password in production)", adminEmail);
        }
    }
}
