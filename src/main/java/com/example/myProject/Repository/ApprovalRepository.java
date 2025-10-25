package com.example.myProject.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.example.myProject.Entity.Approval;

@Repository
public interface ApprovalRepository extends JpaRepository<Approval, Long> {
}