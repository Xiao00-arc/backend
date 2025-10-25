package com.example.myProject.Security;

import com.example.myProject.Entity.User;
import com.example.myProject.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class MyUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));
        
        System.out.println("[MyUserDetailsService] Loading user: " + username);
        System.out.println("[MyUserDetailsService] User role from database: '" + user.getRole() + "'");
        System.out.println("[MyUserDetailsService] User ID: " + user.getId());
        
        // --- THIS IS THE UPDATED LINE ---
        // We now return our custom UserDetails object which includes the user ID
        CustomUserDetails userDetails = new CustomUserDetails(user);
        System.out.println("[MyUserDetailsService] UserDetails authorities: " + userDetails.getAuthorities());
        
        return userDetails;
    }
}