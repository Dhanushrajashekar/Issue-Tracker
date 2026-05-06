package com.issuetracker.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

// JwtFilter runs once per HTTP request, before Spring's own authentication logic.
//
// Flow:
//   1. Read the "Authorization: Bearer <token>" header
//   2. Extract the email from the token
//   3. Load the user from the database
//   4. Validate the token (signature + expiry)
//   5. Tell Spring Security "this user is authenticated" — controllers then see it via Authentication
//
// If no token is present (e.g. login/register requests), we just let the request through.
// SecurityConfig marks those endpoints as public, so they don't need authentication.
@Component
public class JwtFilter extends OncePerRequestFilter {

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");

        // If there's no Authorization header or it doesn't start with "Bearer ", skip JWT logic
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // Strip "Bearer " prefix to get the raw token
        final String jwt = authHeader.substring(7);
        final String email;

        try {
            email = jwtService.extractEmail(jwt);
        } catch (Exception e) {
            // Token is malformed or tampered — let the request through without authentication
            filterChain.doFilter(request, response);
            return;
        }

        // Only proceed if we got an email AND there's no existing authentication (don't re-authenticate)
        if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = userDetailsService.loadUserByUsername(email);

            if (jwtService.isTokenValid(jwt, userDetails)) {
                // Create an authentication token and attach it to the security context
                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // This is what makes the user "authenticated" for the rest of this request
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        filterChain.doFilter(request, response);
    }
}
