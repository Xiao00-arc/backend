package com.example.myProject.Controller;

import java.util.List;

// 1. IMPORT Page, Pageable AND PreAuthorize
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.example.myProject.Entity.ExpenseCategory;
import com.example.myProject.Services.ExpenseCategoryServices;

@RestController
@RequestMapping("/api/expense-categories")
public class ExpenseCategoryController {

    @Autowired
    private ExpenseCategoryServices expenseCategoryService;

    @PostMapping("/post")
    @PreAuthorize("hasRole('ADMIN')")
    public com.example.myProject.Entity.ExpenseCategory createExpenseCategory(@RequestBody com.example.myProject.Entity.ExpenseCategory category) {
        return expenseCategoryService.createExpenseCategory(category);
    }

    // --- THIS IS THE UPDATED ENDPOINT ---
    @GetMapping
    @PreAuthorize("isAuthenticated()") // Any logged-in user can view the list of categories
    public Page<com.example.myProject.Entity.ExpenseCategory> getAllExpenseCategories(Pageable pageable) {
        return expenseCategoryService.getAllExpenseCategories(pageable);
    }
    // ------------------------------------
    
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<com.example.myProject.Entity.ExpenseCategory> getExpenseCategoryById(@PathVariable Long id) {
        return expenseCategoryService.getExpenseCategoryById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<com.example.myProject.Entity.ExpenseCategory> updateExpenseCategory(@PathVariable Long id, @RequestBody com.example.myProject.Entity.ExpenseCategory categoryDetails) {
        com.example.myProject.Entity.ExpenseCategory updatedCategory = expenseCategoryService.updateExpenseCategory(id, categoryDetails);
        if (updatedCategory != null) {
            return ResponseEntity.ok(updatedCategory);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String deleteExpenseCategory(@PathVariable Long id) {
        return expenseCategoryService.deleteExpenseCategory(id);
    }
}