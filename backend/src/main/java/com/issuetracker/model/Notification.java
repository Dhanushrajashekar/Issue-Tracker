package com.issuetracker.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

// A notification is created whenever something significant happens on an issue
// (new comment, status change, assignment change, etc.)
// Stored in the DB and also pushed to the browser in real time via WebSocket.
@Entity
@Table(name = "notifications")
@Data
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // The text shown to the user e.g. "Alice commented on BUG-42: ..."
    @Column(nullable = false)
    private String message;

    // Which user should receive this notification
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "recipient_id", nullable = false)
    @JsonIgnoreProperties({"password", "activationToken", "resetToken", "resetTokenExpiry", "authorities", "enabled", "accountNonExpired", "accountNonLocked", "credentialsNonExpired", "username"})
    private User recipient;

    // ID of the related issue so the frontend can link to it
    private Long relatedIssueId;

    // Stored separately so we don't need a join just to show the notification
    private String relatedIssueTitle;

    // false = unread (shows the red badge), true = user has seen it
    @Column(nullable = false)
    private boolean read = false;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
