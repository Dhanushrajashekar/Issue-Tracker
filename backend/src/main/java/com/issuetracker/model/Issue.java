package com.issuetracker.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.issuetracker.model.enums.IssuePriority;
import com.issuetracker.model.enums.IssueStatus;
import com.issuetracker.model.enums.IssueType;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

// An Issue represents a bug, feature request, task, or improvement within a project.
// This is the central entity of the whole system.
@Entity
@Table(name = "issues")
@Data
public class Issue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    // Stored as a string in the DB so SQL queries can filter by name, not just integer
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private IssueStatus status = IssueStatus.OPEN;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private IssuePriority priority = IssuePriority.MEDIUM;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private IssueType type = IssueType.BUG;

    // Which project this issue belongs to
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "project_id", nullable = false)
    @JsonIgnoreProperties({"members", "description"})
    private Project project;

    // Who filed this issue
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "reporter_id", nullable = false)
    @JsonIgnoreProperties({"password", "activationToken", "resetToken", "resetTokenExpiry", "authorities", "enabled", "accountNonExpired", "accountNonLocked", "credentialsNonExpired", "username"})
    private User reporter;

    // Who is currently responsible for fixing it (can be null = unassigned)
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "assignee_id")
    @JsonIgnoreProperties({"password", "activationToken", "resetToken", "resetTokenExpiry", "authorities", "enabled", "accountNonExpired", "accountNonLocked", "credentialsNonExpired", "username"})
    private User assignee;

    // Users who "watch" this issue and get notified of all updates
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "issue_watchers",
        joinColumns = @JoinColumn(name = "issue_id"),
        inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    @JsonIgnoreProperties({"password", "activationToken", "resetToken", "resetTokenExpiry", "authorities", "enabled", "accountNonExpired", "accountNonLocked", "credentialsNonExpired", "username"})
    private Set<User> watchers = new HashSet<>();

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    // Updated any time the issue is modified — tracked for activity feeds
    private LocalDateTime updatedAt = LocalDateTime.now();

    @PreUpdate
    public void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
