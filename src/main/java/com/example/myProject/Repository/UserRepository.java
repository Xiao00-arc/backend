package com.example.myProject.Repository;

import java.util.Optional;
import java.util.List; // <-- ADD THIS IMPORT
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.example.myProject.Entity.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    Optional<User> findByUsername(String username);

    // --- ADD THIS NEW METHOD ---
    // This finds all users with a specific role. We'll use it to find the Finance Manager.
    List<User> findByRole(String role);
    // -------------------------
}