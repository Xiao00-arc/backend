package com.example.myProject.Controller;

import java.util.List;

// 1. IMPORT Page, Pageable AND PreAuthorize
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.example.myProject.Entity.Department;
import com.example.myProject.Services.DepartmentServices;

@RestController
@RequestMapping("/api/departments")
public class DepartmentController {

    @Autowired
    private DepartmentServices departmentService;

    @PostMapping("/post")
    @PreAuthorize("hasRole('ADMIN')")
    public com.example.myProject.Entity.Department createDepartment(@RequestBody com.example.myProject.Entity.Department department) {
        return departmentService.createDepartment(department);
    }

    // --- THIS IS THE UPDATED ENDPOINT ---
    @GetMapping
    @PreAuthorize("isAuthenticated()") // Any logged-in user can view the list of departments
    public Page<com.example.myProject.Entity.Department> getAllDepartments(Pageable pageable) {
        return departmentService.getAllDepartments(pageable);
    }
    // ------------------------------------

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<com.example.myProject.Entity.Department> getDepartmentById(@PathVariable Long id) {
        return departmentService.getDepartmentById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<com.example.myProject.Entity.Department> updateDepartment(@PathVariable Long id, @RequestBody com.example.myProject.Entity.Department departmentDetails) {
        com.example.myProject.Entity.Department updatedDepartment = departmentService.updateDepartment(id, departmentDetails);
        if (updatedDepartment != null) {
            return ResponseEntity.ok(updatedDepartment);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String deleteDepartment(@PathVariable Long id) {
        return departmentService.deleteDepartment(id);
    }
}