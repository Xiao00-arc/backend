package com.example.myProject.Services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailServices {

    @Autowired
    private JavaMailSender emailSender;

    // Injects the username (your email) from application.properties
    @Value("${spring.mail.username}")
    private String fromEmailAddress;

    /**
     * Sends a simple plain text email.
     * @param to The recipient's email address.
     * @param subject The subject of the email.
     * @param text The body of the email.
     */
    public void sendSimpleMessage(String to, String subject, String text) {
        try {
            SimpleMailMessage message = new SimpleMailMessage(); 
            message.setFrom(fromEmailAddress);
            message.setTo(to); 
            message.setSubject(subject); 
            message.setText(text);
            emailSender.send(message);
            System.out.println("Email sent successfully to " + to);
        } catch (Exception e) {
            // In a real application, you'd use a logger here
            System.err.println("Error sending email to " + to + ": " + e.getMessage());
        }
    }
}