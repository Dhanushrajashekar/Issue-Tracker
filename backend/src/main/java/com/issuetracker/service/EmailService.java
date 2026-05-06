package com.issuetracker.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

// @Async means these methods run in a background thread.
// The HTTP response is returned immediately — the user doesn't wait for the email to send.
@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${app.frontend.url:http://localhost:3000}")
    private String frontendUrl;

    @Async
    public void sendActivationEmail(String to, String name, String token) {
        String link = frontendUrl + "/activate.html?token=" + token;
        sendEmail(to,
            "Activate your Issue Tracker account",
            "Hi " + name + ",\n\n" +
            "Click the link below to activate your account:\n" + link + "\n\n" +
            "This link expires in 24 hours.\n\nThanks,\nIssue Tracker"
        );
    }

    @Async
    public void sendPasswordResetEmail(String to, String name, String token) {
        String link = frontendUrl + "/reset-password.html?token=" + token;
        sendEmail(to,
            "Reset your Issue Tracker password",
            "Hi " + name + ",\n\n" +
            "Click the link below to reset your password (valid for 1 hour):\n" + link + "\n\n" +
            "If you didn't request this, ignore this email.\n\nThanks,\nIssue Tracker"
        );
    }

    @Async
    public void sendIssueNotificationEmail(String to, String name, String message, Long issueId) {
        String link = frontendUrl + "/issue-detail.html?id=" + issueId;
        sendEmail(to,
            "Issue Tracker: " + message,
            "Hi " + name + ",\n\n" + message + "\n\nView issue: " + link + "\n\nThanks,\nIssue Tracker"
        );
    }

    private void sendEmail(String to, String subject, String body) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);
        } catch (Exception e) {
            // Log but don't crash — email failure should never break the main flow
            System.err.println("Failed to send email to " + to + ": " + e.getMessage());
        }
    }
}
