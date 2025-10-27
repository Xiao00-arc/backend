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
        User managerUser = null;
        
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
            
            managerUser = userRepository.save(manager);
            System.out.println("✅ Manager user created: username='manager', email='servampkuro001@gmail.com'");
        } else {
            System.out.println("ℹ️  Manager user already exists.");
            managerUser = userRepository.findByUsername("manager").orElse(null);
        }

        // Create default employee user and assign manager
        if (userRepository.findByUsername("employee").isEmpty()) {
            User employee = new User();
            employee.setUsername("employee");
            employee.setEmail("zenosvalkyre@gmail.com");
            employee.setPassword(passwordEncoder.encode("employee123"));
            employee.setRole("EMPLOYEE");
            employee.setEmployeeId("EMP-001");
            employee.setDepartmentId(null);
            // Assign the manager to this employee
            employee.setManagerId(managerUser != null ? managerUser.getId() : null);
            
            userRepository.save(employee);
            System.out.println("✅ Employee user created: username='employee', email='zenosvalkyre@gmail.com', manager assigned");
        } else {
            System.out.println("ℹ️  Employee user already exists.");
            // Update existing employee to have a manager if they don't have one
            User existingEmployee = userRepository.findByUsername("employee").orElse(null);
            if (existingEmployee != null && existingEmployee.getManagerId() == null && managerUser != null) {
                existingEmployee.setManagerId(managerUser.getId());
                userRepository.save(existingEmployee);
                System.out.println("✅ Updated employee with manager assignment");
            }
        }
        
        System.out.println("========================================");
        System.out.println("📝 Default Users Summary:");
        System.out.println("   Admin    - username: 'admin'    password: 'admin123'    email: 'prasanthdit001@gmail.com'");
        System.out.println("   Manager  - username: 'manager'  password: 'manager123'  email: 'servampkuro001@gmail.com'");
        System.out.println("   Employee - username: 'employee' password: 'employee123' email: 'zenosvalkyre@gmail.com'");
        System.out.println("            - Manager: " + (managerUser != null ? managerUser.getUsername() : "none"));
        System.out.println("========================================");
    }
}
