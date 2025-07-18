package com.example.ex4.config;

import com.example.ex4.models.AppUser;
import com.example.ex4.models.Role;
import com.example.ex4.repositories.AppUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import jakarta.annotation.PostConstruct;

@Configuration
public class DefaultAdminInitializer {

    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostConstruct
    public void createDefaultAdmin() {
        String username = "admin";
        if (!appUserRepository.existsByUsername(username)) {
            AppUser admin = new AppUser();
            admin.setUsername(username);
            admin.setPassword(passwordEncoder.encode("admin123")); // שנה לסיסמה בטוחה יותר
            admin.setEmail("admin@example.com");
            admin.setFirstName("מנהל");
            admin.setLastName("מערכת");
            admin.setRole(Role.ADMIN);
            admin.setMilitaryIdDoc(new byte[0]);
            admin.setRegistrationStatus(com.example.ex4.models.RegistrationStatus.APPROVED);

            appUserRepository.save(admin);
            System.out.println("✅ Admin user created with username: admin");
        }
    }
}
