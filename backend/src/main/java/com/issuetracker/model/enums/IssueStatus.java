package com.issuetracker.model.enums;

// Lifecycle of an issue from creation to closure
public enum IssueStatus {
    OPEN,        // just created, not yet picked up
    IN_PROGRESS, // someone is actively working on it
    IN_REVIEW,   // fix is ready, waiting for code review or QA
    RESOLVED,    // fix is complete and verified
    CLOSED       // no further action needed (won't fix, duplicate, etc.)
}
