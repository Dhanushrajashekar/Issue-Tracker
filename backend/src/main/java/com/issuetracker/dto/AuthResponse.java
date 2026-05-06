package com.issuetracker.dto;

import com.issuetracker.model.User;
import lombok.AllArgsConstructor;
import lombok.Data;

// Returned by the login endpoint.
// The frontend stores the token in localStorage and attaches it to every request.
@Data
@AllArgsConstructor
public class AuthResponse {
    private String token;
    private User user;
    private String tokenType = "Bearer";

    public AuthResponse(String token, User user) {
        this.token = token;
        this.user = user;
    }
}
