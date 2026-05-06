package com.issuetracker.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

// A comment on an issue — used for discussion, status updates, and resolution notes.
@Entity
@Table(name = "comments")
@Data
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    // Which issue this comment belongs to
    // @JsonIgnoreProperties prevents infinite loop: issue → comments → issue → ...
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "issue_id", nullable = false)
    @JsonIgnoreProperties({"comments", "watchers", "description"})
    private Issue issue;

    // Who wrote this comment
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "author_id", nullable = false)
    @JsonIgnoreProperties({"password", "activationToken", "resetToken", "resetTokenExpiry", "authorities", "enabled", "accountNonExpired", "accountNonLocked", "credentialsNonExpired", "username"})
    private User author;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
