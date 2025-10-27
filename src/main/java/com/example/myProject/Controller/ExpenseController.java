package com.example.myProject.Controller;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.myProject.DTO.ExpenseCreateRequest;
import com.example.myProject.Entity.Expense;
import com.example.myProject.Services.ExpenseServices;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/expenses")
public class ExpenseController {

    @Autowired
    private ExpenseServices expenseService;

    // --- ADD THIS NEW ENDPOINT FOR "MY EXPENSES" ---
    @GetMapping("/my-expenses")
    @PreAuthorize("isAuthenticated()") // Any authenticated (logged-in) user can call this
    public Page<com.example.myProject.Entity.Expense> getMyExpenses(Pageable pageable) {
        return expenseService.getMyExpenses(pageable);
    }
    // -----------------------------------------------

    // Inside ExpenseController.java
@PostMapping("/post")
@PreAuthorize("isAuthenticated()")
public Expense createExpense(@Valid @RequestBody ExpenseCreateRequest expenseRequest) {
    return expenseService.createExpense(expenseRequest);
}

    // Endpoint for admins/managers to see ALL expenses
    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('FINANCE_MANAGER')")
    public Page<com.example.myProject.Entity.Expense> getAllExpenses(Pageable pageable) {
        return expenseService.getAllExpenses(pageable);
    }
    
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()") // Any logged-in user can view details of one expense
    public ResponseEntity<com.example.myProject.Entity.Expense> getExpenseById(@PathVariable Long id) {
        return expenseService.getExpenseById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')") // Only admins can generically update an expense record
    public ResponseEntity<com.example.myProject.Entity.Expense> updateExpense(@PathVariable Long id, @Valid @RequestBody com.example.myProject.Entity.Expense expenseDetails) {
        com.example.myProject.Entity.Expense updatedExpense = expenseService.updateExpense(id, expenseDetails);
        if (updatedExpense != null) {
            return ResponseEntity.ok(updatedExpense);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String deleteExpense(@PathVariable Long id) {
        return expenseService.deleteExpense(id);
    }
}