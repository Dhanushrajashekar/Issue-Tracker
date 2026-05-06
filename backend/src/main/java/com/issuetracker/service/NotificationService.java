package com.issuetracker.service;

import com.issuetracker.model.Notification;
import com.issuetracker.model.User;
import com.issuetracker.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NotificationService {

    @Autowired private NotificationRepository notificationRepository;
    @Autowired private SimpMessagingTemplate messagingTemplate;
    @Autowired private EmailService emailService;
    @Autowired private UserService userService;

    // Create a notification, save it, and push it to the browser in real time.
    // The user sees an instant toast popup and the badge count updates.
    public Notification sendNotification(User recipient, String message, Long issueId, String issueTitle) {
        Notification notification = new Notification();
        notification.setRecipient(recipient);
        notification.setMessage(message);
        notification.setRelatedIssueId(issueId);
        notification.setRelatedIssueTitle(issueTitle);

        Notification saved = notificationRepository.save(notification);

        // Push to browser via WebSocket — shows up instantly as a toast
        messagingTemplate.convertAndSendToUser(
                recipient.getEmail(),
                "/queue/notifications",
                saved
        );

        // Also send an email in the background
        emailService.sendIssueNotificationEmail(recipient.getEmail(), recipient.getName(), message, issueId);

        return saved;
    }

    // Notify all watchers of an issue except the person who triggered the action
    public void notifyWatchers(Iterable<User> watchers, Long actorId, String message, Long issueId, String issueTitle) {
        for (User watcher : watchers) {
            if (!watcher.getId().equals(actorId)) {
                sendNotification(watcher, message, issueId, issueTitle);
            }
        }
    }

    public List<Notification> getUserNotifications(String email) {
        User user = userService.findByEmail(email);
        return notificationRepository.findByRecipientOrderByCreatedAtDesc(user);
    }

    public long getUnreadCount(String email) {
        User user = userService.findByEmail(email);
        return notificationRepository.countByRecipientAndReadFalse(user);
    }

    public void markAsRead(Long notificationId, String email) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found"));

        if (!notification.getRecipient().getEmail().equals(email)) {
            throw new RuntimeException("Not your notification");
        }

        notification.setRead(true);
        notificationRepository.save(notification);
    }

    public void markAllAsRead(String email) {
        User user = userService.findByEmail(email);
        List<Notification> unread = notificationRepository.findByRecipientAndReadFalse(user);
        unread.forEach(n -> n.setRead(true));
        notificationRepository.saveAll(unread);
    }
}
