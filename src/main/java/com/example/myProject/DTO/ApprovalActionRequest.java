package com.example.myProject.DTO;

public class ApprovalActionRequest {
    private String status; // Will be "APPROVED" or "REJECTED"
    private String comments;

    public ApprovalActionRequest() {}

    // --- Getters and Setters ---
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getComments() { return comments; }
    public void setComments(String comments) { this.comments = comments; }
}