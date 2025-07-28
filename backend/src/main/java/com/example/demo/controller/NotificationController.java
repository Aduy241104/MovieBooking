package com.example.demo.controller;

import com.example.demo.DTO.request.SendNotificationRequest;
import com.example.demo.DTO.request.SendNotificationToAllRequest;
import com.example.demo.DTO.response.ResAccountAPI;
import com.example.demo.exception.AppException;
import com.example.demo.model.Account;
import com.example.demo.model.Notification;
import com.example.demo.path.NotificationPath;
import com.example.demo.service.AccountService;
import com.example.demo.service.NotificationService;
import com.example.demo.utils.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
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
        if (currentUserId == null) {
            throw new AppException("User not authenticated");
        }
        Account account = accountService.fetchAccountById(Long.parseLong(currentUserId));
        return notificationService.getUserNotifications(account);
    }

    @PostMapping(NotificationPath.MARK_AS_READ)
    public void markAsRead(@PathVariable Long id) {
        notificationService.markAsRead(id);
    }

    @GetMapping(NotificationPath.GET_MY_NOTIFICATIONS)
    public List<Notification> getMyNotificationsByTime(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {
        String currentUserId = SecurityUtils.getCurrentUsername();
        if (currentUserId == null) {
            throw new AppException("User not authenticated");
        }
        Account account = accountService.fetchAccountById(Long.parseLong(currentUserId));
        return notificationService.getUserNotificationsByTime(account, from, to);
    }

    @DeleteMapping(NotificationPath.DELETE_BY_TIME)
    public void deleteNotificationsByTime(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {
        String currentUserId = SecurityUtils.getCurrentUsername();
        if (currentUserId == null) {
            throw new AppException("User not authenticated");
        }
        Account account = accountService.fetchAccountById(Long.parseLong(currentUserId));
        notificationService.deleteNotificationsByTime(account, from, to);
    }

    @DeleteMapping(NotificationPath.DELETE_NOTIFICATION)
    public void deleteNotification(@PathVariable Long id) {
        notificationService.deleteNotification(id);
    }

    @PostMapping(NotificationPath.SEND_NOTIFICATION)
    public ResponseEntity<ResAccountAPI> sendNotification(@RequestBody SendNotificationRequest request) {
        try {
            notificationService.sendNotificationToUsers(
                    request.getAccountIds(),
                    request.getTitle(),
                    request.getContent(),
                    request.getType());
            return ResponseEntity.ok(new ResAccountAPI(true, "Gửi thông báo thành công"));
        } catch (Exception e) {
            return ResponseEntity.ok(new ResAccountAPI(false, "Lỗi khi gửi thông báo: " + e.getMessage()));
        }
    }

    @PostMapping(NotificationPath.SEND_NOTIFICATION_TO_ALL)
    public ResponseEntity<ResAccountAPI> sendNotificationToAll(@RequestBody SendNotificationToAllRequest request) {
        try {
            notificationService.sendNotificationToAllUsers(
                    request.getTitle(),
                    request.getContent(),
                    request.getType());
            return ResponseEntity.ok(new ResAccountAPI(true, "Gửi thông báo đến tất cả người dùng thành công"));
        } catch (Exception e) {
            return ResponseEntity.ok(new ResAccountAPI(false, "Lỗi khi gửi thông báo: " + e.getMessage()));
        }
    }

}
