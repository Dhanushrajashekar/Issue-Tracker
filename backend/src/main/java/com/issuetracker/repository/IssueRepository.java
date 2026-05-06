package com.issuetracker.repository;

import com.issuetracker.model.Issue;
import com.issuetracker.model.Project;
import com.issuetracker.model.User;
import com.issuetracker.model.enums.IssueStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IssueRepository extends JpaRepository<Issue, Long> {

    // All issues in a project, newest first
    List<Issue> findByProjectOrderByCreatedAtDesc(Project project);

    // Issues assigned to a specific developer
    List<Issue> findByAssigneeOrderByCreatedAtDesc(User assignee);

    // Issues filed by a specific user
    List<Issue> findByReporterOrderByCreatedAtDesc(User reporter);

    // Issues a user is watching
    List<Issue> findByWatchersContaining(User user);

    // Filter by project + status (used for kanban columns)
    List<Issue> findByProjectAndStatus(Project project, IssueStatus status);

    // Count open issues in a project — used on the dashboard stat cards
    long countByProjectAndStatus(Project project, IssueStatus status);
}
