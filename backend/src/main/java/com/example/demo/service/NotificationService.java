package com.example.demo.service;

import com.example.demo.model.Account;
import com.example.demo.model.Notification;
import com.example.demo.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NotificationService {
    @Autowired
    private NotificationRepository notificationRepository;
    @Autowired
    private SimpMessagingTemplate messagingTemplate;

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
        messagingTemplate.convertAndSend("/queue/notify-" + account.getAccountId(), notification);
    }

    public List<Notification> getUserNotifications(Account account) {
        return notificationRepository.findByAccountOrderByCreatedAtDesc(account);
    }

    public void markAsRead(Long notificationId) {
        notificationRepository.findById(notificationId).ifPresent(n -> {
            n.setIsRead(true);
            notificationRepository.save(n);
        });
    }
}
