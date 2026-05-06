package com.issuetracker.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.issuetracker.model.enums.Role;
import jakarta.persistence.*;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

// User implements UserDetails so Spring Security can use it directly for authentication.
// That way we don't need a separate wrapper class.
@Entity
@Table(name = "users")
@Data
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @JsonIgnore // never send the password hash over the API
    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING) // store "ROLE_ADMIN" not "0"
    @Column(nullable = false)
    private Role role = Role.ROLE_DEVELOPER; // default role for new users

    @Column(nullable = false)
    private boolean active = false;

    @JsonIgnore
    private String activationToken;

    @JsonIgnore
    private String resetToken;

    @JsonIgnore
    private LocalDateTime resetTokenExpiry;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    // Spring Security: return the role as a granted authority
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(role.name()));
    }

    // Spring Security uses email as the "username"
    @Override
    public String getUsername() {
        return email;
    }

    // Spring Security checks these — we manage activation ourselves via the active field
    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled() { return active; }
}
