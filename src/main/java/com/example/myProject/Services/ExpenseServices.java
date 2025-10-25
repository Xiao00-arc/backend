package com.example.myProject.Services;

import java.math.BigDecimal;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.example.myProject.DTO.ExpenseCreateRequest; // <-- 1. IMPORT THE NEW DTO
import com.example.myProject.Entity.Approval;
import com.example.myProject.Entity.Expense;
import com.example.myProject.Entity.User;
import com.example.myProject.Repository.ExpenseRepository;
import com.example.myProject.Repository.UserRepository;

@Service
public class ExpenseServices {

    @Autowired
    private ExpenseRepository expenseRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ApprovalServices approvalService;
    @Autowired
    private EmailServices emailService;

    public Page<Expense> getMyExpenses(Pageable pageable) {
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new RuntimeException("Authenticated user not found in database"));
        return expenseRepository.findByEmployeeId(currentUser.getId(), pageable);
    }

    // --- THIS IS THE UPDATED METHOD ---
    public Expense createExpense(ExpenseCreateRequest expenseRequest) {
        // Create a new, full Expense entity from the simple request DTO
        Expense newExpense = new Expense();
        newExpense.setEmployeeId(expenseRequest.getEmployeeId());
        newExpense.setAmount(expenseRequest.getAmount());
        newExpense.setDescription(expenseRequest.getDescription());
        newExpense.setExpenseDate(expenseRequest.getExpenseDate());
        newExpense.setCategoryId(expenseRequest.getCategoryId());
        newExpense.setStatus("PENDING"); // Set the default status

        // First, save the new expense to the database so it gets an ID
        Expense savedExpense = expenseRepository.save(newExpense);

        // Now, run the existing approval workflow logic
        BigDecimal managerApprovalThreshold = new BigDecimal("100.00");
        if (savedExpense.getAmount().compareTo(managerApprovalThreshold) >= 0) {
            Optional<User> employeeOptional = userRepository.findById(savedExpense.getEmployeeId());

            if (employeeOptional.isPresent() && employeeOptional.get().getManagerId() != null) {
                User employee = employeeOptional.get();
                Long managerId = employee.getManagerId();
                User manager = userRepository.findById(managerId).orElse(null);

                if (manager != null) {
                    Approval newApproval = new Approval();
                    newApproval.setExpenseId(savedExpense.getId());
                    newApproval.setApproverId(managerId);
                    newApproval.setApprovalStatus("PENDING");
                    approvalService.createApproval(newApproval);

                    String subject = "New Expense Approval Request";
                    String text = String.format(
                        "Hello %s,\n\nA new expense claim for $%s submitted by %s is awaiting your approval.\n\nDescription: %s\n\nPlease log in to the system to review it.",
                        manager.getUsername(), savedExpense.getAmount(), employee.getUsername(), savedExpense.getDescription()
                    );
                    emailService.sendSimpleMessage(manager.getEmail(), subject, text);
                }
            }
        }
        return savedExpense;
    }
    // ------------------------------------
    
    public Page<Expense> getAllExpenses(Pageable pageable) { return expenseRepository.findAll(pageable); }
    public Optional<Expense> getExpenseById(Long id) { return expenseRepository.findById(id); }
    public Expense updateExpense(Long id, Expense expenseDetails) {
        Expense existingExpense = expenseRepository.findById(id).orElse(null);
        if (existingExpense != null) {
            existingExpense.setEmployeeId(expenseDetails.getEmployeeId());
            existingExpense.setAmount(expenseDetails.getAmount());
            existingExpense.setDescription(expenseDetails.getDescription());
            existingExpense.setExpenseDate(expenseDetails.getExpenseDate());
            existingExpense.setCategoryId(expenseDetails.getCategoryId());
            existingExpense.setStatus(expenseDetails.getStatus());
            existingExpense.setPaymentAmount(expenseDetails.getPaymentAmount());
            existingExpense.setPaymentDate(expenseDetails.getPaymentDate());
            existingExpense.setPaymentMethod(expenseDetails.getPaymentMethod());
            existingExpense.setTransactionId(expenseDetails.getTransactionId());
            return expenseRepository.save(existingExpense);
        }
        return null;
    }
    public String deleteExpense(Long id) {
        if (expenseRepository.existsById(id)) {
            expenseRepository.deleteById(id);
            return "Expense with ID " + id + " has been deleted.";
        } else {
            return "Expense with ID " + id + " not found.";
        }
    }
}