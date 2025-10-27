package com.example.myProject.Services;

import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.example.myProject.Entity.User;
import com.example.myProject.Repository.UserRepository;
import com.example.myProject.DTO.UserSignUpRequest; // <-- 1. IMPORT THE DTO

@Service
public class UserServices {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    // --- THIS IS THE UPDATED METHOD ---
    public User createUser(UserSignUpRequest signUpRequest) {
        // Create a new, full User entity from the simple request object
        User newUser = new User();
        newUser.setUsername(signUpRequest.getUsername());
        newUser.setEmail(signUpRequest.getEmail());
        newUser.setPassword(passwordEncoder.encode(signUpRequest.getPassword()));

        // --- Set the default values for new sign-ups ---
        newUser.setRole("EMPLOYEE"); // All new signups are Employees by default
        newUser.setEmployeeId("EMP-" + System.currentTimeMillis()); // Generate a simple, unique employee ID
        
        // Automatically assign default manager to new employees
        User defaultManager = userRepository.findByUsername("manager").orElse(null);
        if (defaultManager != null) {
            newUser.setManagerId(defaultManager.getId());
            System.out.println("✅ Assigned default manager (ID: " + defaultManager.getId() + ") to new employee: " + newUser.getUsername());
        } else {
            // If no default manager exists, try to find any user with MANAGER role
            List<User> managers = userRepository.findByRole("MANAGER");
            if (!managers.isEmpty()) {
                newUser.setManagerId(managers.get(0).getId());
                System.out.println("✅ Assigned manager (ID: " + managers.get(0).getId() + ") to new employee: " + newUser.getUsername());
            } else {
                newUser.setManagerId(null);
                System.out.println("⚠️  No manager found - new employee has no manager assigned");
            }
        }
        
        newUser.setDepartmentId(null);

        // Save the complete User entity to the database
        return userRepository.save(newUser);
    }
    
    // --- CREATE USER WITH SPECIFIC ROLE (for admins only) ---
    public User createUserWithRole(UserSignUpRequest signUpRequest, String role) {
        User newUser = new User();
        newUser.setUsername(signUpRequest.getUsername());
        newUser.setEmail(signUpRequest.getEmail());
        newUser.setPassword(passwordEncoder.encode(signUpRequest.getPassword()));
        
        // Set the specified role (ADMIN, MANAGER, EMPLOYEE)
        newUser.setRole(role.toUpperCase());
        newUser.setEmployeeId("EMP-" + System.currentTimeMillis());
        
        newUser.setDepartmentId(null);
        newUser.setManagerId(null);
        
        return userRepository.save(newUser);
    }
    // ------------------------------------

    // ... (Your other methods like getAllUsers, updateUser, etc., remain the same) ...
    public Page<User> getAllUsers(Pageable pageable) { 
        return userRepository.findAll(pageable); 
    }
    public Optional<User> getUserById(Long id) { 
        return userRepository.findById(id); 
    }
    public User updateUser(Long id, User userDetails) {
        User existingUser = userRepository.findById(id).orElse(null);
        if (existingUser != null) {
            existingUser.setUsername(userDetails.getUsername());
            existingUser.setEmail(userDetails.getEmail());
            existingUser.setRole(userDetails.getRole());
            existingUser.setEmployeeId(userDetails.getEmployeeId());
            existingUser.setDepartmentId(userDetails.getDepartmentId());
            existingUser.setManagerId(userDetails.getManagerId());
            if (userDetails.getPassword() != null && !userDetails.getPassword().isEmpty()) {
                existingUser.setPassword(passwordEncoder.encode(userDetails.getPassword()));
            }
            return userRepository.save(existingUser);
        }
        return null;
    }
    public String deleteUser(Long id) {
        if (userRepository.existsById(id)) {
            userRepository.deleteById(id);
            return "User with ID " + id + " has been deleted.";
        } else {
            return "User with ID " + id + " not found.";
        }
    }
}