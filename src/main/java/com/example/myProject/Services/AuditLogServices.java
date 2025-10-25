package com.example.myProject.Services;

import java.util.List;
import java.util.Optional;

// 1. IMPORT Page AND Pageable
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.example.myProject.Entity.AuditLog;
import com.example.myProject.Repository.AuditLogRepository;

@Service
public class AuditLogServices {

    @Autowired
    private AuditLogRepository auditLogRepository;

    public AuditLog createAuditLog(AuditLog auditLog) {
        return auditLogRepository.save(auditLog);
    }

    // --- THIS IS THE UPDATED METHOD ---
    public Page<AuditLog> getAllAuditLogs(Pageable pageable) {
        return auditLogRepository.findAll(pageable);
    }
    // --------------------------------

    public Optional<AuditLog> getAuditLogById(Long id) {
        return auditLogRepository.findById(id);
    }

    public AuditLog updateAuditLog(Long id, AuditLog logDetails) {
        AuditLog existingLog = auditLogRepository.findById(id).orElse(null);
        if (existingLog != null) {
            existingLog.setUserId(logDetails.getUserId());
            existingLog.setAction(logDetails.getAction());
            existingLog.setEntityType(logDetails.getEntityType());
            existingLog.setEntityId(logDetails.getEntityId());
            existingLog.setTimestamp(logDetails.getTimestamp());
            return auditLogRepository.save(existingLog);
        }
        return null;
    }

    public String deleteAuditLog(Long id) {
        if (auditLogRepository.existsById(id)) {
            auditLogRepository.deleteById(id);
            return "AuditLog with ID " + id + " has been deleted.";
        } else {
            return "AuditLog with ID " + id + " not found.";
        }
    }
}