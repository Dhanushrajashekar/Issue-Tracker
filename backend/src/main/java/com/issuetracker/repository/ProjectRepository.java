package com.issuetracker.repository;

import com.issuetracker.model.Project;
import com.issuetracker.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProjectRepository extends JpaRepository<Project, Long> {

    // Get all projects where this user is a member
    List<Project> findByMembersContaining(User user);

    // Get all projects owned by this user
    List<Project> findByOwner(User owner);

    // Check that no two projects share the same short key (e.g. "PROJ")
    boolean existsByProjectKey(String projectKey);

    Optional<Project> findByProjectKey(String projectKey);
}
