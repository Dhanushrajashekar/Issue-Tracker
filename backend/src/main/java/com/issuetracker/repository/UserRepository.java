package com.issuetracker.repository;

import com.issuetracker.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

// Spring Data JPA auto-generates all the SQL for these method signatures.
// No implementation needed — Spring does it at runtime.
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    Optional<User> findByActivationToken(String token);

    Optional<User> findByResetToken(String token);

    boolean existsByEmail(String email);
}
