package com.issuetracker.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

// Request body for POST /api/projects
@Data
public class CreateProjectRequest {

    @NotBlank(message = "Project name is required")
    @Size(max = 100)
    private String name;

    private String description;

    // Key must be 2–10 uppercase letters, e.g. "PROJ", "BUG", "AUTH"
    @NotBlank(message = "Project key is required")
    @Pattern(regexp = "^[A-Z]{2,10}$", message = "Project key must be 2–10 uppercase letters (e.g. PROJ)")
    private String projectKey;
}
