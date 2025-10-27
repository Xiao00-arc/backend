package com.example.myProject.Controller;

import java.util.List;
import java.io.IOException;
import org.springframework.core.io.Resource;
import jakarta.servlet.http.HttpServletRequest;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders; // <-- ADD THIS
import org.springframework.http.MediaType; // <-- ADD THIS
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.example.myProject.Entity.Receipt;
import com.example.myProject.Services.ReceiptServices;
import com.example.myProject.Services.FileStorageService;

@RestController
@RequestMapping("/api/receipts")
public class ReceiptController {

    @Autowired
    private ReceiptServices receiptService;

    @Autowired
    private FileStorageService fileStorageService;

    @PostMapping("/upload")
    @PreAuthorize("isAuthenticated()")
    public com.example.myProject.Entity.Receipt uploadReceipt(@RequestParam("file") MultipartFile file, @RequestParam("expenseId") Long expenseId) {
        String fileName = fileStorageService.store(file);
        Receipt newReceipt = new Receipt();
        newReceipt.setExpenseId(expenseId);
        newReceipt.setFileName(fileName);
        String filePath = "./uploads/" + fileName;
        newReceipt.setFilePath(filePath);
        return receiptService.createReceipt(newReceipt);
    }

    // --- THIS IS THE NEW DOWNLOAD ENDPOINT ---
    @GetMapping("/files/{filename:.+}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Resource> serveFile(@PathVariable String filename, HttpServletRequest request) {
        // Load file as a Resource from the service
        Resource resource = fileStorageService.loadAsResource(filename);

        // Try to determine file's content type
        String contentType = "application/octet-stream"; // Fallback
        try {
            contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
        } catch (IOException ex) {
            // Log this error if you want
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }
    // ------------------------------------

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'FINANCE_MANAGER')")
    public Page<com.example.myProject.Entity.Receipt> getAllReceipts(Pageable pageable) {
        return receiptService.getAllReceipts(pageable);
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<com.example.myProject.Entity.Receipt> getReceiptById(@PathVariable Long id) {
        return receiptService.getReceiptById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<com.example.myProject.Entity.Receipt> updateReceipt(@PathVariable Long id, @RequestBody com.example.myProject.Entity.Receipt receiptDetails) {
        com.example.myProject.Entity.Receipt updatedReceipt = receiptService.updateReceipt(id, receiptDetails);
        if (updatedReceipt != null) {
            return ResponseEntity.ok(updatedReceipt);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String deleteReceipt(@PathVariable Long id) {
        return receiptService.deleteReceipt(id);
    }

    @GetMapping("/expense/{expenseId}")
    @PreAuthorize("isAuthenticated()")
    public List<Receipt> getReceiptsByExpenseId(@PathVariable Long expenseId) {
        return receiptService.getReceiptsByExpenseId(expenseId);
    }
}