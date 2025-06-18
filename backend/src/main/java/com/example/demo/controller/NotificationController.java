package com.example.demo.controller;


import com.example.demo.model.Account;
import com.example.demo.model.Notification;
import com.example.demo.service.AccountService;
import com.example.demo.service.NotificationService;
import com.example.demo.utils.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {
    @Autowired
    private NotificationService notificationService;
    @Autowired
    private AccountService accountService;

    @GetMapping
    public List<Notification> getMyNotifications() {
        String currentUserId = SecurityUtils.getCurrentUsername();
        if(currentUserId == null) {
            throw new RuntimeException("User not authenticated");
        }
        Account account = accountService.fetchAccountById(Long.parseLong(currentUserId));
        return notificationService.getUserNotifications(account);
    }

    @PostMapping("/{id}/read")
    public void markAsRead(@PathVariable Long id) {
        notificationService.markAsRead(id);
    }
}
