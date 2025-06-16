package com.example.demo.controller;


import com.example.demo.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/notification")
public class NotificationController {
    @Autowired
    private NotificationService notificationService;
}
