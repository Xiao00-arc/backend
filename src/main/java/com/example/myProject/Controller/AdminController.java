package com.example.myProject.Controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.myProject.Entity.User;
import com.example.myProject.Repository.UserRepository;
import com.example.myProject.Repository.ExpenseRepository;
import com.example.myProject.Repository.ApprovalRepository;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private ExpenseRepository expenseRepository;
    
    @Autowired
    private ApprovalRepository approvalRepository;

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
    
    @GetMapping("/check-employees")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> checkEmployees() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            List<User> allUsers = userRepository.findAll();
            List<Map<String, Object>> userList = new java.util.ArrayList<>();
            
            for (User user : allUsers) {
                Map<String, Object> userInfo = new HashMap<>();
                userInfo.put("id", user.getId());
                userInfo.put("username", user.getUsername());
                userInfo.put("email", user.getEmail());
                userInfo.put("role", user.getRole());
                userInfo.put("employeeId", user.getEmployeeId());
                userInfo.put("managerId", user.getManagerId());
                userInfo.put("hasManager", user.getManagerId() != null);
                userList.add(userInfo);
            }
            
            response.put("success", true);
            response.put("totalUsers", allUsers.size());
            response.put("users", userList);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    @PostMapping("/create-missing-approvals")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> createMissingApprovals() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Get all expenses
            List<com.example.myProject.Entity.Expense> allExpenses = 
                new java.util.ArrayList<>(expenseRepository.findAll());
            
            // Get all existing approval expense IDs
            List<com.example.myProject.Entity.Approval> allApprovals = approvalRepository.findAll();
            java.util.Set<Long> approvedExpenseIds = new java.util.HashSet<>();
            for (com.example.myProject.Entity.Approval approval : allApprovals) {
                approvedExpenseIds.add(approval.getExpenseId());
            }
            
            int createdCount = 0;
            java.util.List<String> createdApprovals = new java.util.ArrayList<>();
            
            // Find the default manager
            User defaultManager = userRepository.findByUsername("manager").orElse(null);
            if (defaultManager == null) {
                List<User> managers = userRepository.findByRole("MANAGER");
                if (!managers.isEmpty()) {
                    defaultManager = managers.get(0);
                }
            }
            
            if (defaultManager == null) {
                response.put("success", false);
                response.put("message", "No manager found in the system");
                return ResponseEntity.badRequest().body(response);
            }
            
            // Create approval records for expenses that don't have them
            for (com.example.myProject.Entity.Expense expense : allExpenses) {
                if (!approvedExpenseIds.contains(expense.getId())) {
                    // This expense doesn't have an approval record, create one
                    com.example.myProject.Entity.Approval newApproval = new com.example.myProject.Entity.Approval();
                    newApproval.setExpenseId(expense.getId());
                    newApproval.setApproverId(defaultManager.getId());
                    newApproval.setApprovalStatus("PENDING");
                    
                    approvalRepository.save(newApproval);
                    createdCount++;
                    
                    String info = String.format("Expense ID: %d, Amount: $%.2f, Description: %s", 
                        expense.getId(), expense.getAmount(), expense.getDescription());
                    createdApprovals.add(info);
                    
                    System.out.println("✅ Created approval for expense " + expense.getId());
                }
            }
            
            response.put("success", true);
            response.put("message", "Created " + createdCount + " approval records");
            response.put("totalExpenses", allExpenses.size());
            response.put("existingApprovals", allApprovals.size());
            response.put("createdApprovals", createdCount);
            response.put("details", createdApprovals);
            response.put("managerId", defaultManager.getId());
            response.put("managerUsername", defaultManager.getUsername());
            
            System.out.println("========================================");
            System.out.println("✅ Missing Approvals Created");
            System.out.println("   Total Expenses: " + allExpenses.size());
            System.out.println("   Existing Approvals: " + allApprovals.size());
            System.out.println("   Created Approvals: " + createdCount);
            System.out.println("========================================");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "Error: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
}
