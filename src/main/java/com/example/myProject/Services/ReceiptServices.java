package com.example.myProject.Services;

import java.util.List;
import java.util.Optional;

// 1. IMPORT Page AND Pageable
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.example.myProject.Entity.Receipt;
import com.example.myProject.Repository.ReceiptRepository;

@Service
public class ReceiptServices {

    @Autowired
    private ReceiptRepository receiptRepository;

    public Receipt createReceipt(Receipt receipt) {
        return receiptRepository.save(receipt);
    }

    // --- THIS IS THE UPDATED METHOD ---
    public Page<Receipt> getAllReceipts(Pageable pageable) {
        return receiptRepository.findAll(pageable);
    }
    // --------------------------------

    public Optional<Receipt> getReceiptById(Long id) {
        return receiptRepository.findById(id);
    }

    public Receipt updateReceipt(Long id, Receipt receiptDetails) {
        Receipt existingReceipt = receiptRepository.findById(id).orElse(null);
        if (existingReceipt != null) {
            existingReceipt.setExpenseId(receiptDetails.getExpenseId());
            existingReceipt.setFileName(receiptDetails.getFileName());
            existingReceipt.setFilePath(receiptDetails.getFilePath());
            existingReceipt.setOcrText(receiptDetails.getOcrText());
            return receiptRepository.save(existingReceipt);
        }
        return null;
    }

    public String deleteReceipt(Long id) {
        if (receiptRepository.existsById(id)) {
            receiptRepository.deleteById(id);
            return "Receipt with ID " + id + " has been deleted.";
        } else {
            return "Receipt with ID " + id + " not found.";
        }
    }

    public List<Receipt> getReceiptsByExpenseId(Long expenseId) {
        return receiptRepository.findByExpenseId(expenseId);
    }
}