package com.issuetracker.model.enums;

// How urgently the issue needs to be addressed
public enum IssuePriority {
    LOW,      // nice to have, no deadline pressure
    MEDIUM,   // should be done soon but not blocking anything
    HIGH,     // blocking work or affecting many users
    CRITICAL  // production is down, all hands on deck
}
