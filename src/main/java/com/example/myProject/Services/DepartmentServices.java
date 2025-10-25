package com.example.myProject.Services;

import java.util.List;
import java.util.Optional;

// 1. IMPORT Page AND Pageable
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.example.myProject.Entity.Department;
import com.example.myProject.Repository.DepartmentRepository;

@Service
public class DepartmentServices {

    @Autowired
    private DepartmentRepository departmentRepository;

    public Department createDepartment(Department department) {
        return departmentRepository.save(department);
    }

    // --- THIS IS THE UPDATED METHOD ---
    public Page<Department> getAllDepartments(Pageable pageable) {
        return departmentRepository.findAll(pageable);
    }
    // --------------------------------

    public Optional<Department> getDepartmentById(Long id) {
        return departmentRepository.findById(id);
    }

    public Department updateDepartment(Long id, Department departmentDetails) {
        Department existingDepartment = departmentRepository.findById(id).orElse(null);
        if (existingDepartment != null) {
            existingDepartment.setDepartmentName(departmentDetails.getDepartmentName());
            existingDepartment.setDepartmentCode(departmentDetails.getDepartmentCode());
            return departmentRepository.save(existingDepartment);
        }
        return null;
    }

    public String deleteDepartment(Long id) {
        if (departmentRepository.existsById(id)) {
            departmentRepository.deleteById(id);
            return "Department with ID " + id + " has been deleted.";
        } else {
            return "Department with ID " + id + " not found.";
        }
    }
}