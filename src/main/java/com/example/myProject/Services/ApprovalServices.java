package com.example.myProject.Services;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.example.myProject.DTO.ApprovalActionRequest;
import com.example.myProject.Entity.Approval;
import com.example.myProject.Entity.Department;
import com.example.myProject.Entity.Expense;
import com.example.myProject.Entity.User;
import com.example.myProject.Repository.ApprovalRepository;
import com.example.myProject.Repository.DepartmentRepository;
import com.example.myProject.Repository.ExpenseRepository;
import com.example.myProject.Repository.UserRepository;

@Service
public class ApprovalServices {

    private static final BigDecimal FINANCE_APPROVAL_THRESHOLD = new BigDecimal("1000.00");
    private static final BigDecimal DEPT_HEAD_APPROVAL_THRESHOLD = new BigDecimal("5000.00");

    @Autowired
    private ApprovalRepository approvalRepository;
    @Autowired
    private ExpenseRepository expenseRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private DepartmentRepository departmentRepository;
    
    @Autowired
    private EmailServices emailService; // <-- 1. INJECT EmailServices

    public Approval processApprovalAction(Long approvalId, ApprovalActionRequest actionRequest) {
        Approval approval = approvalRepository.findById(approvalId)
                .orElseThrow(() -> new RuntimeException("Approval not found with id: " + approvalId));

        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByUsername(currentUsername).orElseThrow();
        
        System.out.println("[ApprovalServices] Processing approval action:");
        System.out.println("[ApprovalServices] Current user: " + currentUsername + " (ID: " + currentUser.getId() + ")");
        System.out.println("[ApprovalServices] Current user role: " + currentUser.getRole());
        System.out.println("[ApprovalServices] Approval ID: " + approvalId);
        System.out.println("[ApprovalServices] Assigned approver ID: " + approval.getApproverId());
        
        // Enhanced authorization logic: Allow admins, managers, and finance managers to approve any expense
        // Or allow the specifically assigned approver to approve
        boolean isAuthorized = false;
        
        // Check if user is admin/manager/finance manager (can approve any expense)
        if ("ADMIN".equals(currentUser.getRole()) || 
            "MANAGER".equals(currentUser.getRole()) || 
            "FINANCE_MANAGER".equals(currentUser.getRole())) {
            isAuthorized = true;
            System.out.println("[ApprovalServices] User authorized as " + currentUser.getRole());
        }
        // Check if user is the specifically assigned approver
        else if (approval.getApproverId() != null && approval.getApproverId().equals(currentUser.getId())) {
            isAuthorized = true;
            System.out.println("[ApprovalServices] User authorized as assigned approver");
        }
        
        if (!isAuthorized) {
            System.out.println("[ApprovalServices] User NOT authorized - Role: " + currentUser.getRole() + ", Assigned approver: " + approval.getApproverId());
            throw new SecurityException("You are not authorized to process this request.");
        }
        
        System.out.println("[ApprovalServices] Authorization successful, processing approval...");

        approval.setApprovalStatus(actionRequest.getStatus());
        approval.setComments(actionRequest.getComments());
        approval.setApprovalDate(LocalDateTime.now());
        approvalRepository.save(approval);

        Expense expense = expenseRepository.findById(approval.getExpenseId()).orElseThrow();
        User employee = userRepository.findById(expense.getEmployeeId()).orElseThrow();
        String finalStatus = null; // Used to track if we should notify the employee

        if ("REJECTED".equalsIgnoreCase(actionRequest.getStatus())) {
            expense.setStatus("REJECTED");
            finalStatus = "REJECTED";
        } else if ("APPROVED".equalsIgnoreCase(actionRequest.getStatus())) {
            boolean isFinalApproval = determineNextStep(expense, currentUser);
            if (isFinalApproval) {
                expense.setStatus("APPROVED");
                finalStatus = "APPROVED";
            }
        }
        
        expenseRepository.save(expense);
        
        // --- 2. SEND EMAIL NOTIFICATION TO THE EMPLOYEE IF THE PROCESS IS FINISHED ---
        if (finalStatus != null) {
            String subject = "Update on your Expense Claim #" + expense.getId();
            String text = String.format(
                "Hello %s,\n\nYour expense claim for $%s ('%s') has been %s.\n\nApprover comments: %s",
                employee.getUsername(),
                expense.getAmount(),
                expense.getDescription(),
                finalStatus.toLowerCase(),
                actionRequest.getComments() != null ? actionRequest.getComments() : "N/A"
            );
            emailService.sendSimpleMessage(employee.getEmail(), subject, text);
        }
        // -------------------------------------------------------------------------

        return approval;
    }

    private boolean determineNextStep(Expense expense, User currentApprover) {
        BigDecimal amount = expense.getAmount();

        switch (currentApprover.getRole()) {
            case "MANAGER":
                if (amount.compareTo(FINANCE_APPROVAL_THRESHOLD) > 0) {
                    escalateToRole("FINANCE_MANAGER", expense);
                    return false;
                }
                return true;

            case "FINANCE_MANAGER":
                if (amount.compareTo(DEPT_HEAD_APPROVAL_THRESHOLD) > 0) {
                    escalateToDeptHead(expense);
                    return false;
                }
                return true;

            default:
                return true;
        }
    }

    private void escalateToRole(String role, Expense expense) {
        List<User> nextApprovers = userRepository.findByRole(role);
        if (nextApprovers.isEmpty()) {
            throw new RuntimeException("No user found with role: " + role + " to escalate approval.");
        }
        User nextApprover = nextApprovers.get(0);

        Approval nextApproval = new Approval();
        nextApproval.setExpenseId(expense.getId());
        nextApproval.setApproverId(nextApprover.getId());
        nextApproval.setApprovalStatus("PENDING");
        approvalRepository.save(nextApproval);

        // --- 3. SEND EMAIL NOTIFICATION TO THE NEXT APPROVER ---
        String subject = "New Expense Approval Request (Escalated)";
        String text = String.format(
            "Hello %s,\n\nAn expense claim for $%s has been escalated and is awaiting your approval.",
            nextApprover.getUsername(),
            expense.getAmount()
        );
        emailService.sendSimpleMessage(nextApprover.getEmail(), subject, text);
        // ----------------------------------------------------
    }

    private void escalateToDeptHead(Expense expense) {
        User employee = userRepository.findById(expense.getEmployeeId()).orElseThrow();
        Department department = departmentRepository.findById(employee.getDepartmentId()).orElseThrow();
        Long deptHeadId = department.getManagerId();

        if (deptHeadId == null) {
            throw new RuntimeException("Department head not found for department: " + department.getDepartmentName());
        }
        
        User deptHead = userRepository.findById(deptHeadId).orElseThrow();
        
        Approval nextApproval = new Approval();
        nextApproval.setExpenseId(expense.getId());
        nextApproval.setApproverId(deptHead.getId());
        nextApproval.setApprovalStatus("PENDING");
        approvalRepository.save(nextApproval);

        // --- 4. SEND EMAIL NOTIFICATION TO THE DEPT HEAD ---
        String subject = "New Expense Approval Request (Escalated)";
        String text = String.format(
            "Hello %s,\n\nAn expense claim for $%s has been escalated and is awaiting your approval.",
            deptHead.getUsername(),
            expense.getAmount()
        );
        emailService.sendSimpleMessage(deptHead.getEmail(), subject, text);
        // ----------------------------------------------------
    }

    // ... (Your other methods like createApproval, etc., remain the same) ...
    public Approval createApproval(Approval approval) { return approvalRepository.save(approval); }
    public Page<Approval> getAllApprovals(Pageable pageable) { return approvalRepository.findAll(pageable); }
    public Optional<Approval> getApprovalById(Long id) { return approvalRepository.findById(id); }
    public String deleteApproval(Long id) {
        if (approvalRepository.existsById(id)) {
            approvalRepository.deleteById(id);
            return "Approval with ID " + id + " has been deleted.";
        } else {
            return "Approval with ID " + id + " not found.";
        }
    }
}