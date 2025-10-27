package com.example.myProject.Config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.example.myProject.Entity.User;
import com.example.myProject.Repository.UserRepository;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // Check if admin user already exists
        if (userRepository.findByUsername("admin").isEmpty()) {
            // Create default admin user
            User admin = new User();
            admin.setUsername("admin");
            admin.setEmail("admin@example.com");
            admin.setPassword(passwordEncoder.encode("admin123")); // Change this password!
            admin.setRole("ADMIN");
            admin.setEmployeeId("EMP-ADMIN-001");
            admin.setDepartmentId(null);
            admin.setManagerId(null);
            
            userRepository.save(admin);
            System.out.println("✅ Default admin user created: username='admin', password='admin123'");
            System.out.println("⚠️  IMPORTANT: Please change the admin password after first login!");
        } else {
            System.out.println("ℹ️  Admin user already exists, skipping initialization.");
        }
    }
}
