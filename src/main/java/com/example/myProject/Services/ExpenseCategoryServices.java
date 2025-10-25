package com.example.myProject.Services;

import java.util.List;
import java.util.Optional;

// 1. IMPORT Page AND Pageable
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.example.myProject.Entity.ExpenseCategory;
import com.example.myProject.Repository.ExpenseCategoryRepository;

@Service
public class ExpenseCategoryServices {

    @Autowired
    private ExpenseCategoryRepository expenseCategoryRepository;

    public ExpenseCategory createExpenseCategory(ExpenseCategory category) {
        return expenseCategoryRepository.save(category);
    }

    // --- THIS IS THE UPDATED METHOD ---
    public Page<ExpenseCategory> getAllExpenseCategories(Pageable pageable) {
        return expenseCategoryRepository.findAll(pageable);
    }
    // --------------------------------

    public Optional<ExpenseCategory> getExpenseCategoryById(Long id) {
        return expenseCategoryRepository.findById(id);
    }

    public ExpenseCategory updateExpenseCategory(Long id, ExpenseCategory categoryDetails) {
        ExpenseCategory existingCategory = expenseCategoryRepository.findById(id).orElse(null);
        if (existingCategory != null) {
            existingCategory.setCategoryName(categoryDetails.getCategoryName());
            existingCategory.setCategoryCode(categoryDetails.getCategoryCode());
            existingCategory.setSpendingLimit(categoryDetails.getSpendingLimit());
            return expenseCategoryRepository.save(existingCategory);
        }
        return null;
    }

    public String deleteExpenseCategory(Long id) {
        if (expenseCategoryRepository.existsById(id)) {
            expenseCategoryRepository.deleteById(id);
            return "ExpenseCategory with ID " + id + " has been deleted.";
        } else {
            return "ExpenseCategory with ID " + id + " not found.";
        }
    }
}