package com.issuetracker.dto;

import com.issuetracker.model.enums.IssuePriority;
import com.issuetracker.model.enums.IssueType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

// Request body for POST /api/issues
@Data
public class CreateIssueRequest {

    @NotBlank(message = "Title is required")
    private String title;

    private String description;

    @NotNull(message = "Project ID is required")
    private Long projectId;

    private Long assigneeId; // optional, can be unassigned

    private IssuePriority priority = IssuePriority.MEDIUM;

    private IssueType type = IssueType.BUG;
}
