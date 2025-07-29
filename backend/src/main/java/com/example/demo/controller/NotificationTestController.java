//package com.example.demo.controller;
//
//import com.example.demo.model.Account;
//import com.example.demo.repository.AccountRepository;
//import com.example.demo.service.NotificationService;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//@RestController
//@RequestMapping("/api/test")
//@CrossOrigin(origins = "http://localhost:3000")
//public class NotificationTestController {
//
//    @Autowired
//    private NotificationService notificationService;
//
//    @Autowired
//    private AccountRepository accountRepository;
//
//    @PostMapping("/send-notification/{accountId}")
//    public ResponseEntity<String> sendTestNotification(@PathVariable Long accountId) {
//        Account account = accountRepository.findById(accountId).orElse(null);
//        if (account == null) {
//            return ResponseEntity.badRequest().body("Account not found");
//        }
//
//        notificationService.notify(account, "Test Notification", "This is a test notification from backend", "test");
//        return ResponseEntity.ok("Notification sent successfully");
//    }
//}
