package com.example.myProject.Controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/debug")
public class DebugController {

    @GetMapping("/auth-info")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> getAuthInfo() {
        System.out.println("=== DEBUG ENDPOINT CALLED ===");
        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Map<String, Object> authInfo = new HashMap<>();
        
        if (auth != null) {
            authInfo.put("principal", auth.getPrincipal().toString());
            authInfo.put("authorities", auth.getAuthorities().toString());
            authInfo.put("isAuthenticated", auth.isAuthenticated());
            authInfo.put("name", auth.getName());
            
            System.out.println("Authentication Principal: " + auth.getPrincipal());
            System.out.println("Authentication Authorities: " + auth.getAuthorities());
            System.out.println("Is Authenticated: " + auth.isAuthenticated());
        } else {
            authInfo.put("error", "No authentication found");
            System.out.println("No authentication found in SecurityContext");
        }
        
        System.out.println("=== END DEBUG ENDPOINT ===");
        return ResponseEntity.ok(authInfo);
    }
    
    @GetMapping("/admin-test")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> adminTest() {
        return ResponseEntity.ok("Admin access successful!");
    }
    
    @GetMapping("/manager-test")  
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'FINANCE_MANAGER')")
    public ResponseEntity<String> managerTest() {
        return ResponseEntity.ok("Manager access successful!");
    }
}