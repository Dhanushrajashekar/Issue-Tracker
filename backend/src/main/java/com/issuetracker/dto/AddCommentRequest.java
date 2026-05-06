package com.issuetracker.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

// Request body for POST /api/issues/{id}/comments
@Data
public class AddCommentRequest {
    @NotBlank(message = "Comment content cannot be empty")
    private String content;
}
