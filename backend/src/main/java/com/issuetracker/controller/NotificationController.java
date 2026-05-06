package com.issuetracker.controller;

import com.issuetracker.dto.ApiResponse;
import com.issuetracker.model.Notification;
import com.issuetracker.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    @Autowired private NotificationService notificationService;

    @GetMapping
    public ResponseEntity<?> getNotifications(Authentication auth) {
        List<Notification> notifications = notificationService.getUserNotifications(auth.getName());
        long unreadCount = notificationService.getUnreadCount(auth.getName());
        return ResponseEntity.ok(Map.of("notifications", notifications, "unreadCount", unreadCount));
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<?> markRead(@PathVariable Long id, Authentication auth) {
        try {
            notificationService.markAsRead(id, auth.getName());
            return ResponseEntity.ok(new ApiResponse(true, "Marked as read"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage()));
        }
    }

    @PutMapping("/read-all")
    public ResponseEntity<?> markAllRead(Authentication auth) {
        notificationService.markAllAsRead(auth.getName());
        return ResponseEntity.ok(new ApiResponse(true, "All notifications marked as read"));
    }
}
