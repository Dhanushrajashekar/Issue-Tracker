package com.issuetracker.model.enums;

// The three roles in the system. Spring Security expects role names to start with ROLE_.
// ADMIN  → full access (manage users, delete anything, view all projects)
// DEVELOPER → create/update issues, upload attachments, add comments
// REPORTER → create issues and comments, view assigned projects
public enum Role {
    ROLE_ADMIN,
    ROLE_DEVELOPER,
    ROLE_REPORTER
}
