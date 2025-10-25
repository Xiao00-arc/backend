package com.example.myProject.Controller;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.example.myProject.Entity.User;
import com.example.myProject.Services.UserServices;
import com.example.myProject.DTO.UserSignUpRequest; // <-- 1. IMPORT THE DTO
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserServices userService;

    // --- THIS ENDPOINT IS NOW SECURED AND USES THE DTO ---
    @PostMapping("/post")
    @PreAuthorize("hasRole('ADMIN')") // Only an Admin can create new users
    public com.example.myProject.Entity.User createUser(@Valid @RequestBody UserSignUpRequest signUpRequest) {
        return userService.createUser(signUpRequest);
    }
    // ---------------------------------------------------

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Page<com.example.myProject.Entity.User> getAllUsers(Pageable pageable) {
        return userService.getAllUsers(pageable);
    }

    @GetMapping("/{id}")
    // Any authenticated user can view a user's profile, but you could restrict this further
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<com.example.myProject.Entity.User> getUserById(@PathVariable Long id) {
        return userService.getUserById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // --- THIS ENDPOINT IS NOW SECURED ---
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')") // Only an Admin can update user details
    public ResponseEntity<com.example.myProject.Entity.User> updateUser(@PathVariable Long id, @Valid @RequestBody com.example.myProject.Entity.User userDetails) {
        com.example.myProject.Entity.User updatedUser = userService.updateUser(id, userDetails);
        if (updatedUser != null) {
            return ResponseEntity.ok(updatedUser);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    // ------------------------------------

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String deleteUser(@PathVariable Long id) {
        return userService.deleteUser(id);
    }
}