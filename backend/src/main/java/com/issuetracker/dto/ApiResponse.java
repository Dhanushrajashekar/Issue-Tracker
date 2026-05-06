package com.issuetracker.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

// Generic success/error response for endpoints that don't return data
// e.g. { "success": true, "message": "Account created" }
@Data
@AllArgsConstructor
public class ApiResponse {
    private boolean success;
    private String message;
    private Object data;

    public ApiResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }
}
