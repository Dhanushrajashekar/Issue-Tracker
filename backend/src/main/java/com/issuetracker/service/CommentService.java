package com.issuetracker.service;

import com.issuetracker.model.Comment;
import com.issuetracker.model.Issue;
import com.issuetracker.model.User;
import com.issuetracker.repository.CommentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CommentService {

    @Autowired private CommentRepository commentRepository;
    @Autowired private IssueService issueService;
    @Autowired private UserService userService;
    @Autowired private NotificationService notificationService;

    public Comment addComment(Long issueId, String content, String authorEmail) {
        Issue issue = issueService.findById(issueId);
        User author = userService.findByEmail(authorEmail);

        Comment comment = new Comment();
        comment.setContent(content);
        comment.setIssue(issue);
        comment.setAuthor(author);

        Comment saved = commentRepository.save(comment);

        // Notify all watchers that a new comment was added
        String msg = author.getName() + " commented on \"" + issue.getTitle() + "\": "
                + content.substring(0, Math.min(content.length(), 60))
                + (content.length() > 60 ? "…" : "");

        notificationService.notifyWatchers(issue.getWatchers(), author.getId(), msg, issue.getId(), issue.getTitle());

        // If the commenter isn't already watching, add them so they see replies
        issue.getWatchers().add(author);

        return saved;
    }

    public List<Comment> getIssueComments(Long issueId) {
        Issue issue = issueService.findById(issueId);
        return commentRepository.findByIssueOrderByCreatedAtAsc(issue);
    }

    public void deleteComment(Long commentId, String requesterEmail) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found"));

        User requester = userService.findByEmail(requesterEmail);
        boolean isAuthor = comment.getAuthor().getEmail().equals(requesterEmail);
        boolean isAdmin = requester.getRole().name().equals("ROLE_ADMIN");

        if (!isAuthor && !isAdmin) {
            throw new RuntimeException("You can only delete your own comments");
        }

        commentRepository.delete(comment);
    }
}
