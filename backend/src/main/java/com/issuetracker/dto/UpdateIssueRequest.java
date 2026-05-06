package com.issuetracker.dto;

import com.issuetracker.model.enums.IssuePriority;
import com.issuetracker.model.enums.IssueStatus;
import com.issuetracker.model.enums.IssueType;
import lombok.Data;

// Request body for PUT /api/issues/{id}
// All fields are optional — only non-null values are applied (partial update)
@Data
public class UpdateIssueRequest {
    private String title;
    private String description;
    private IssueStatus status;
    private IssuePriority priority;
    private IssueType type;
    private Long assigneeId;    // null = leave unchanged, -1 = unassign
}
