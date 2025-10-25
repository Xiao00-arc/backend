package com.example.myProject.Controller;

import java.util.List;

// 1. IMPORT Page, Pageable AND PreAuthorize
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.example.myProject.Entity.AuditLog;
import com.example.myProject.Services.AuditLogServices;

@RestController
@RequestMapping("/api/audit-logs")
public class AuditLogController {

    @Autowired
    private AuditLogServices auditLogService;

    // Creating logs should ideally be an internal process, but if exposed, restrict to ADMIN
    @PostMapping("/post")
    @PreAuthorize("hasRole('ADMIN')")
    public com.example.myProject.Entity.AuditLog createAuditLog(@RequestBody com.example.myProject.Entity.AuditLog auditLog) {
        return auditLogService.createAuditLog(auditLog);
    }

    // --- THIS IS THE UPDATED ENDPOINT ---
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'AUDITOR', 'FINANCE_MANAGER')")
    public Page<com.example.myProject.Entity.AuditLog> getAllAuditLogs(Pageable pageable) {
        return auditLogService.getAllAuditLogs(pageable);
    }
    // ------------------------------------
    
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'AUDITOR', 'FINANCE_MANAGER')")
    public ResponseEntity<com.example.myProject.Entity.AuditLog> getAuditLogById(@PathVariable Long id) {
        return auditLogService.getAuditLogById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Updating logs is highly unusual, so it should be restricted to ADMINs
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<com.example.myProject.Entity.AuditLog> updateAuditLog(@PathVariable Long id, @RequestBody com.example.myProject.Entity.AuditLog logDetails) {
        com.example.myProject.Entity.AuditLog updatedLog = auditLogService.updateAuditLog(id, logDetails);
        if (updatedLog != null) {
            return ResponseEntity.ok(updatedLog);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    
    // Deleting logs is a very sensitive action, restrict to ADMIN only
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String deleteAuditLog(@PathVariable Long id) {
        return auditLogService.deleteAuditLog(id);
    }
}   