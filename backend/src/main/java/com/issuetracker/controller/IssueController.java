package com.issuetracker.controller;

import com.issuetracker.dto.ApiResponse;
import com.issuetracker.dto.CreateIssueRequest;
import com.issuetracker.dto.UpdateIssueRequest;
import com.issuetracker.model.Issue;
import com.issuetracker.service.IssueService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/issues")
public class IssueController {

    @Autowired private IssueService issueService;

    // Create a new issue
    @PostMapping
    public ResponseEntity<?> createIssue(@Valid @RequestBody CreateIssueRequest request, Authentication auth) {
        try {
            Issue issue = issueService.createIssue(request, auth.getName());
            return ResponseEntity.status(HttpStatus.CREATED).body(issue);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage()));
        }
    }

    // Get a single issue by ID
    @GetMapping("/{id}")
    public ResponseEntity<?> getIssue(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(issueService.findById(id));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse(false, e.getMessage()));
        }
    }

    // Get all issues in a project
    @GetMapping("/project/{projectId}")
    public ResponseEntity<?> getProjectIssues(@PathVariable Long projectId, Authentication auth) {
        try {
            List<Issue> issues = issueService.getProjectIssues(projectId, auth.getName());
            return ResponseEntity.ok(issues);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage()));
        }
    }

    // Get issues assigned to me
    @GetMapping("/my/assigned")
    public ResponseEntity<List<Issue>> getMyAssigned(Authentication auth) {
        return ResponseEntity.ok(issueService.getMyAssignedIssues(auth.getName()));
    }

    // Get issues I reported
    @GetMapping("/my/reported")
    public ResponseEntity<List<Issue>> getMyReported(Authentication auth) {
        return ResponseEntity.ok(issueService.getMyReportedIssues(auth.getName()));
    }

    // Update issue fields (status, priority, assignee, etc.)
    @PutMapping("/{id}")
    public ResponseEntity<?> updateIssue(@PathVariable Long id,
                                          @RequestBody UpdateIssueRequest request,
                                          Authentication auth) {
        try {
            Issue issue = issueService.updateIssue(id, request, auth.getName());
            return ResponseEntity.ok(issue);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteIssue(@PathVariable Long id, Authentication auth) {
        try {
            issueService.deleteIssue(id, auth.getName());
            return ResponseEntity.ok(new ApiResponse(true, "Issue deleted"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage()));
        }
    }

    // Watch an issue — start receiving notifications for it
    @PostMapping("/{id}/watch")
    public ResponseEntity<?> watch(@PathVariable Long id, Authentication auth) {
        return ResponseEntity.ok(issueService.watchIssue(id, auth.getName()));
    }

    // Unwatch — stop receiving notifications
    @DeleteMapping("/{id}/watch")
    public ResponseEntity<?> unwatch(@PathVariable Long id, Authentication auth) {
        return ResponseEntity.ok(issueService.unwatchIssue(id, auth.getName()));
    }
}
