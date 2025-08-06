package com.example.demo.service;

import com.example.demo.model.Account;
import com.example.demo.model.Notification;
import com.example.demo.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class NotificationService {
    @Autowired
    private NotificationRepository notificationRepository;
    @Autowired
    private SimpMessagingTemplate messagingTemplate;
    @Autowired
    @Lazy
    private AccountService accountService;

    /**
     * Notify a single user with a notification.
     *
     * @param account The account to notify.
     * @param title   The title of the notification.
     * @param content The content of the notification.
     * @param type    The type of the notification.
     */
    public void notify(Account account, String title, String content, String type) {
        Notification notification = Notification.builder()
                .account(account)
                .title(title)
                .content(content)
                .type(type)
                .isRead(false)
                .build();
        notificationRepository.save(notification);

        // Gửi realtime tới client
        String topic = "/queue/notify-" + account.getAccountId();
        System.out.println("Sending notification to topic: " + topic);
        System.out.println("Notification content: " + notification.getTitle() + " - " + notification.getContent());
        messagingTemplate.convertAndSend(topic, notification);
    }

    /**
     * Send notification to multiple users.
     *
     * @param accountIds List of account IDs to notify.
     * @param title      The title of the notification.
     * @param content    The content of the notification.
     * @param type       The type of the notification.
     */
    @Transactional
    public void sendNotificationToUsers(List<Long> accountIds, String title, String content, String type) {
        for (Long accountId : accountIds) {
            Account account = accountService.fetchAccountById(accountId);
            if (account != null) {
                notify(account, title, content, type);
            }
        }
    }

    /**
     * Send notification to all users.
     *
     * @param title   The title of the notification.
     * @param content The content of the notification.
     * @param type    The type of the notification.
     */
    @Transactional
    public void sendNotificationToAllUsers(String title, String content, String type) {
        List<Account> allAccounts = accountService.getAllAccount();
        for (Account account : allAccounts) {
            notify(account, title, content, type);
        }
    }

    /**
     * Get notifications for a specific user.
     *
     * @param account The account to get notifications for.
     * @return List of notifications for the user.
     */
    public List<Notification> getUserNotifications(Account account) {
        return notificationRepository.findByAccountOrderByCreatedAtDesc(account);
    }

    /**
     * Get notifications for a user within a specific time range.
     *
     * @param account The account to get notifications for.
     * @param from    Start time of the range (inclusive).
     * @param to      End time of the range (inclusive).
     * @return List of notifications for the user within the specified time range.
     */
    public List<Notification> getUserNotificationsByTime(Account account, LocalDateTime from, LocalDateTime to) {
        if (from == null && to == null) {
            return getUserNotifications(account);
        }
        if (from != null && to != null) {
            return notificationRepository.findByAccountAndCreatedAtBetweenOrderByCreatedAtDesc(account, from, to);
        }
        if (from != null) {
            return notificationRepository.findByAccountAndCreatedAtAfterOrderByCreatedAtDesc(account, from);
        }
        return notificationRepository.findByAccountAndCreatedAtBeforeOrderByCreatedAtDesc(account, to);
    }

    /**
     * Mark a notification as read.
     *
     * @param notificationId The ID of the notification to mark as read.
     */
    public void markAsRead(Long notificationId) {
        notificationRepository.findById(notificationId).ifPresent(n -> {
            n.setIsRead(true);
            notificationRepository.save(n);
        });
    }

    /**
     * Delete a notification by its ID.
     *
     * @param id The ID of the notification to delete.
     */
    public void deleteNotification(Long id) {
        notificationRepository.deleteById(id);
    }

    /**
     * Delete notifications for a user within a specific time range.
     *
     * @param account The account to delete notifications for.
     * @param from    Start time of the range (inclusive).
     * @param to      End time of the range (inclusive).
     */
    @Transactional
    public void deleteNotificationsByTime(Account account, LocalDateTime from, LocalDateTime to) {
        Long accountId = account.getAccountId();
        if (from != null && to != null) {
            notificationRepository.deleteAllByAccountIdAndCreatedAtBetween(accountId, from, to);
        } else if (from != null) {
            notificationRepository.deleteAllByAccountIdAndCreatedAtAfter(accountId, from);
        } else if (to != null) {
            notificationRepository.deleteAllByAccountIdAndCreatedAtBefore(accountId, to);
        } else {
            notificationRepository.deleteAllByAccountId(accountId);
        }
    }
}
