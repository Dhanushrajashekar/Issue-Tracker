package com.issuetracker.service;

import com.issuetracker.dto.CreateIssueRequest;
import com.issuetracker.dto.UpdateIssueRequest;
import com.issuetracker.model.Issue;
import com.issuetracker.model.Project;
import com.issuetracker.model.User;
import com.issuetracker.repository.IssueRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class IssueService {

    @Autowired private IssueRepository issueRepository;
    @Autowired private ProjectService projectService;
    @Autowired private UserService userService;
    @Autowired private NotificationService notificationService;

    public Issue createIssue(CreateIssueRequest request, String reporterEmail) {
        Project project = projectService.findById(request.getProjectId());
        User reporter = userService.findByEmail(reporterEmail);

        // Only project members can create issues
        if (!projectService.isMember(project, reporterEmail)) {
            throw new RuntimeException("You are not a member of this project");
        }

        Issue issue = new Issue();
        issue.setTitle(request.getTitle());
        issue.setDescription(request.getDescription());
        issue.setPriority(request.getPriority());
        issue.setType(request.getType());
        issue.setProject(project);
        issue.setReporter(reporter);

        if (request.getAssigneeId() != null) {
            issue.setAssignee(userService.findById(request.getAssigneeId()));
        }

        // The reporter automatically watches their own issue
        issue.getWatchers().add(reporter);

        Issue saved = issueRepository.save(issue);

        // Notify the assignee if one was set
        if (saved.getAssignee() != null && !saved.getAssignee().getId().equals(reporter.getId())) {
            notificationService.sendNotification(
                saved.getAssignee(),
                reporter.getName() + " assigned you to: " + saved.getTitle(),
                saved.getId(),
                saved.getTitle()
            );
        }

        return saved;
    }

    public Issue updateIssue(Long issueId, UpdateIssueRequest request, String updaterEmail) {
        Issue issue = findById(issueId);
        User updater = userService.findByEmail(updaterEmail);

        String oldStatus = issue.getStatus().name();

        // Apply only the fields that were provided (partial update)
        if (request.getTitle() != null) issue.setTitle(request.getTitle());
        if (request.getDescription() != null) issue.setDescription(request.getDescription());
        if (request.getStatus() != null) issue.setStatus(request.getStatus());
        if (request.getPriority() != null) issue.setPriority(request.getPriority());
        if (request.getType() != null) issue.setType(request.getType());

        if (request.getAssigneeId() != null) {
            if (request.getAssigneeId() == -1) {
                issue.setAssignee(null); // explicitly unassign
            } else {
                User newAssignee = userService.findById(request.getAssigneeId());
                issue.setAssignee(newAssignee);
                // Notify the new assignee
                if (!newAssignee.getId().equals(updater.getId())) {
                    notificationService.sendNotification(
                        newAssignee,
                        updater.getName() + " assigned you to: " + issue.getTitle(),
                        issue.getId(),
                        issue.getTitle()
                    );
                }
            }
        }

        Issue saved = issueRepository.save(issue);

        // Notify all watchers if the status changed
        if (request.getStatus() != null && !request.getStatus().name().equals(oldStatus)) {
            String msg = updater.getName() + " changed status of \"" + issue.getTitle()
                    + "\" from " + oldStatus + " → " + request.getStatus().name();
            notificationService.notifyWatchers(issue.getWatchers(), updater.getId(), msg, issue.getId(), issue.getTitle());
        }

        return saved;
    }

    public void deleteIssue(Long issueId, String requesterEmail) {
        Issue issue = findById(issueId);
        User requester = userService.findByEmail(requesterEmail);

        // Only the reporter, project owner, or admin can delete
        boolean isReporter = issue.getReporter().getEmail().equals(requesterEmail);
        boolean isProjectOwner = issue.getProject().getOwner().getEmail().equals(requesterEmail);
        boolean isAdmin = requester.getRole().name().equals("ROLE_ADMIN");

        if (!isReporter && !isProjectOwner && !isAdmin) {
            throw new RuntimeException("You don't have permission to delete this issue");
        }

        issueRepository.delete(issue);
    }

    public Issue findById(Long id) {
        return issueRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Issue not found: " + id));
    }

    public List<Issue> getProjectIssues(Long projectId, String requesterEmail) {
        Project project = projectService.findById(projectId);
        if (!projectService.isMember(project, requesterEmail)) {
            throw new RuntimeException("You are not a member of this project");
        }
        return issueRepository.findByProjectOrderByCreatedAtDesc(project);
    }

    public List<Issue> getMyAssignedIssues(String email) {
        User user = userService.findByEmail(email);
        return issueRepository.findByAssigneeOrderByCreatedAtDesc(user);
    }

    public List<Issue> getMyReportedIssues(String email) {
        User user = userService.findByEmail(email);
        return issueRepository.findByReporterOrderByCreatedAtDesc(user);
    }

    // Watch an issue — adds the user to the watchers set so they get notifications
    public Issue watchIssue(Long issueId, String email) {
        Issue issue = findById(issueId);
        User user = userService.findByEmail(email);
        issue.getWatchers().add(user);
        return issueRepository.save(issue);
    }

    // Unwatch — remove from watchers so they stop getting notifications
    public Issue unwatchIssue(Long issueId, String email) {
        Issue issue = findById(issueId);
        User user = userService.findByEmail(email);
        issue.getWatchers().removeIf(w -> w.getId().equals(user.getId()));
        return issueRepository.save(issue);
    }
}
