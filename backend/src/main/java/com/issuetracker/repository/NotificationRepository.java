package com.issuetracker.repository;

import com.issuetracker.model.Notification;
import com.issuetracker.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    // All notifications for a user, newest first
    List<Notification> findByRecipientOrderByCreatedAtDesc(User recipient);

    // Unread notifications — used to compute the badge count
    List<Notification> findByRecipientAndReadFalse(User recipient);

    // Count unread — cheaper than loading the full list just for the count
    long countByRecipientAndReadFalse(User recipient);
}
