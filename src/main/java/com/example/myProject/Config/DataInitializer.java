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
        // Create default admin user
        if (userRepository.findByUsername("admin").isEmpty()) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setEmail("prasanthdit001@gmail.com");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setRole("ADMIN");
            admin.setEmployeeId("EMP-ADMIN-001");
            admin.setDepartmentId(null);
            admin.setManagerId(null);
            
            userRepository.save(admin);
            System.out.println("✅ Admin user created: username='admin', email='prasanthdit001@gmail.com'");
        } else {
            System.out.println("ℹ️  Admin user already exists.");
        }

        // Create default manager user
        if (userRepository.findByUsername("manager").isEmpty()) {
            User manager = new User();
            manager.setUsername("manager");
            manager.setEmail("servampkuro001@gmail.com");
            manager.setPassword(passwordEncoder.encode("manager123"));
            manager.setRole("MANAGER");
            manager.setEmployeeId("EMP-MGR-001");
            manager.setDepartmentId(null);
            manager.setManagerId(null);
            
            userRepository.save(manager);
            System.out.println("✅ Manager user created: username='manager', email='servampkuro001@gmail.com'");
        } else {
            System.out.println("ℹ️  Manager user already exists.");
        }

        // Create default employee user
        if (userRepository.findByUsername("employee").isEmpty()) {
            User employee = new User();
            employee.setUsername("employee");
            employee.setEmail("zenosvalkyre@gmail.com");
            employee.setPassword(passwordEncoder.encode("employee123"));
            employee.setRole("EMPLOYEE");
            employee.setEmployeeId("EMP-001");
            employee.setDepartmentId(null);
            employee.setManagerId(null);
            
            userRepository.save(employee);
            System.out.println("✅ Employee user created: username='employee', email='zenosvalkyre@gmail.com'");
        } else {
            System.out.println("ℹ️  Employee user already exists.");
        }
        
        System.out.println("========================================");
        System.out.println("📝 Default Users Summary:");
        System.out.println("   Admin    - username: 'admin'    password: 'admin123'    email: 'prasanthdit001@gmail.com'");
        System.out.println("   Manager  - username: 'manager'  password: 'manager123'  email: 'servampkuro001@gmail.com'");
        System.out.println("   Employee - username: 'employee' password: 'employee123' email: 'zenosvalkyre@gmail.com'");
        System.out.println("========================================");
    }
}
