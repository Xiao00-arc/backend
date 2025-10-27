package com.example.myProject.Repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.example.myProject.Entity.Approval;

@Repository
public interface ApprovalRepository extends JpaRepository<Approval, Long> {
    // Find all approvals for a specific expense
    List<Approval> findByExpenseId(Long expenseId);
    
    // Find pending approvals for a specific expense
    List<Approval> findByExpenseIdAndApprovalStatus(Long expenseId, String status);
    
    // Find the latest approval for an expense
    Optional<Approval> findFirstByExpenseIdOrderByIdDesc(Long expenseId);
}