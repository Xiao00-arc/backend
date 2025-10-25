package com.example.myProject.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.example.myProject.Entity.Department;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, Long> {
}