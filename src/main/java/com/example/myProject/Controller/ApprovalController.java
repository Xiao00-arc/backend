package com.example.myProject.Controller;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.myProject.DTO.ApprovalActionRequest; // <-- IMPORT new DTO
import com.example.myProject.Entity.Approval;
import com.example.myProject.Services.ApprovalServices;

@RestController
@RequestMapping("/api/approvals")
public class ApprovalController {

    @Autowired
    private ApprovalServices approvalService;

    // --- THIS IS THE NEW, SPECIFIC ENDPOINT FOR APPROVING/REJECTING ---
    @PutMapping("/{id}/action")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'FINANCE_MANAGER')")
    public ResponseEntity<Approval> performApprovalAction(
            @PathVariable Long id,
            @RequestBody ApprovalActionRequest actionRequest) {
        
        System.out.println("=== APPROVAL CONTROLLER DEBUG ===");
        System.out.println("Approval ID: " + id);
        System.out.println("Action Request: " + actionRequest.getStatus() + " - " + actionRequest.getComments());
        
        // Get current authentication details
        org.springframework.security.core.Authentication auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        System.out.println("Current Authentication: " + auth);
        if (auth != null) {
            System.out.println("Principal: " + auth.getPrincipal());
            System.out.println("Authorities: " + auth.getAuthorities());
            System.out.println("Is Authenticated: " + auth.isAuthenticated());
        }
        System.out.println("=== END APPROVAL CONTROLLER DEBUG ===");
        
        Approval updatedApproval = approvalService.processApprovalAction(id, actionRequest);
        return ResponseEntity.ok(updatedApproval);
    }
    // ---------------------------------------------------------------
    
    // This endpoint is now less important but can be kept for admin overrides
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')") // Let's restrict this to only Admins
    public ResponseEntity<com.example.myProject.Entity.Approval> updateApproval(@PathVariable Long id, @RequestBody com.example.myProject.Entity.Approval approvalDetails) {
        // NOTE: The 'processApprovalAction' endpoint should be preferred by managers.
        // This generic update is now just for administrative fixes.
        Approval existingApproval = approvalService.getApprovalById(id).orElse(null);
        if (existingApproval != null) {
            existingApproval.setApprovalStatus(approvalDetails.getApprovalStatus());
            existingApproval.setComments(approvalDetails.getComments());
            // ... set other fields for admin override if needed
            approvalService.createApproval(existingApproval); // Re-using create as a save
            return ResponseEntity.ok(existingApproval);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // ... (Your other endpoints remain mostly the same) ...
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE_MANAGER')")
    public Page<com.example.myProject.Entity.Approval> getAllApprovals(Pageable pageable) {
        return approvalService.getAllApprovals(pageable);
    }
    
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<com.example.myProject.Entity.Approval> getApprovalById(@PathVariable Long id) {
        return approvalService.getApprovalById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String deleteApproval(@PathVariable Long id) {
        return approvalService.deleteApproval(id);
    }

    // Get approvals for a specific expense
    @GetMapping("/expense/{expenseId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<Approval>> getApprovalsByExpenseId(@PathVariable Long expenseId) {
        List<Approval> approvals = approvalService.getApprovalsByExpenseId(expenseId);
        return ResponseEntity.ok(approvals);
    }
}