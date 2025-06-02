package com.example.demo.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.DTO.response.ApiResponse;
import com.example.demo.Service.AccountService;
import com.example.demo.model.Account;

import org.springframework.web.bind.annotation.GetMapping;
import lombok.extern.slf4j.Slf4j;

@CrossOrigin(origins = "*")
@RestController
@Slf4j
@RequestMapping("/api/public")
public class AccountController {

    @Autowired
    private AccountService accountService;

    @GetMapping("/acc")
    public ApiResponse<List<Account>> getMethodName() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();

        log.info("User info: {}", authentication.getName());
        authentication.getAuthorities().forEach(grantedAuthority -> log.info(grantedAuthority.getAuthority()));

        List<Account> accounts = accountService.getAllAccount();
        return ApiResponse.<List<Account>>builder()
                .result(accounts)
                .build();
    }

}
