package com.issuetracker.repository;

import com.issuetracker.model.Comment;
import com.issuetracker.model.Issue;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    // All comments on an issue, oldest first (chronological thread order)
    List<Comment> findByIssueOrderByCreatedAtAsc(Issue issue);

    // Count comments — shown on the issue list as a quick stat
    long countByIssue(Issue issue);
}
