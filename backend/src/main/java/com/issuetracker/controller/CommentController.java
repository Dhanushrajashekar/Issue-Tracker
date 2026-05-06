package com.issuetracker.controller;

import com.issuetracker.dto.AddCommentRequest;
import com.issuetracker.dto.ApiResponse;
import com.issuetracker.model.Comment;
import com.issuetracker.service.CommentService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class CommentController {

    @Autowired private CommentService commentService;

    @PostMapping("/issues/{issueId}/comments")
    public ResponseEntity<?> addComment(@PathVariable Long issueId,
                                        @Valid @RequestBody AddCommentRequest request,
                                        Authentication auth) {
        try {
            Comment comment = commentService.addComment(issueId, request.getContent(), auth.getName());
            return ResponseEntity.status(HttpStatus.CREATED).body(comment);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage()));
        }
    }

    @GetMapping("/issues/{issueId}/comments")
    public ResponseEntity<List<Comment>> getComments(@PathVariable Long issueId) {
        return ResponseEntity.ok(commentService.getIssueComments(issueId));
    }

    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<?> deleteComment(@PathVariable Long commentId, Authentication auth) {
        try {
            commentService.deleteComment(commentId, auth.getName());
            return ResponseEntity.ok(new ApiResponse(true, "Comment deleted"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage()));
        }
    }
}
