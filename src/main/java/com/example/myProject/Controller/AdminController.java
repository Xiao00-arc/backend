package com.example.myProject.Controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.myProject.Entity.User;
import com.example.myProject.Repository.UserRepository;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/assign-managers")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> assignManagersToEmployees() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Find the default manager
            User defaultManager = userRepository.findByUsername("manager").orElse(null);
            
            if (defaultManager == null) {
                // Try to find any manager
                List<User> managers = userRepository.findByRole("MANAGER");
                if (!managers.isEmpty()) {
                    defaultManager = managers.get(0);
                } else {
                    response.put("success", false);
                    response.put("message", "No manager found in the system");
                    return ResponseEntity.badRequest().body(response);
                }
            }
            
            // Find all employees without managers
            List<User> allUsers = userRepository.findAll();
            int updatedCount = 0;
            
            for (User user : allUsers) {
                // If user is an employee and has no manager
                if ("EMPLOYEE".equals(user.getRole()) && user.getManagerId() == null) {
                    user.setManagerId(defaultManager.getId());
                    userRepository.save(user);
                    updatedCount++;
                    System.out.println("✅ Assigned manager to employee: " + user.getUsername() + " (ID: " + user.getId() + ")");
                }
            }
            
            response.put("success", true);
            response.put("message", "Successfully assigned manager to " + updatedCount + " employees");
            response.put("managerId", defaultManager.getId());
            response.put("managerUsername", defaultManager.getUsername());
            response.put("updatedCount", updatedCount);
            
            System.out.println("========================================");
            System.out.println("✅ Manager Assignment Complete");
            System.out.println("   Manager: " + defaultManager.getUsername() + " (ID: " + defaultManager.getId() + ")");
            System.out.println("   Employees Updated: " + updatedCount);
            System.out.println("========================================");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
}
