package com.issuetracker.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

// A file attached to an issue (screenshots, logs, specs, etc.)
// Files are stored on disk (dev) or in S3 (prod).
@Entity
@Table(name = "attachments")
@Data
public class Attachment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // The original filename the user uploaded (e.g., "screenshot.png")
    @Column(nullable = false)
    private String originalName;

    // The name we store the file as on disk — uses a UUID so filenames never collide
    @Column(nullable = false, unique = true)
    private String storedName;

    // The MIME type (e.g., "image/png", "application/pdf")
    private String contentType;

    // File size in bytes — shown to users
    private long fileSize;

    // Which issue this file is attached to
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "issue_id", nullable = false)
    @JsonIgnoreProperties({"attachments", "watchers", "description"})
    private Issue issue;

    // Who uploaded it
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "uploader_id", nullable = false)
    @JsonIgnoreProperties({"password", "activationToken", "resetToken", "resetTokenExpiry", "authorities", "enabled", "accountNonExpired", "accountNonLocked", "credentialsNonExpired", "username"})
    private User uploader;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
