package com.example.demo.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.DTO.response.ApiResponse;
import com.example.demo.DTO.response.AuthRespond;
import com.example.demo.service.AuthenticationService;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/auth")
public class GoogleAuthController {

    @Autowired
    AuthenticationService authenticationService;

    @PostMapping("/google-login")
    public ApiResponse<AuthRespond> postMethodName(@RequestBody Map<String, String> request) {
        String idToken = request.get("idToken");
        AuthRespond authRespond = authenticationService.handleLoginWithGoogle(idToken);
        return ApiResponse.<AuthRespond>builder()
                .message("success")
                .result(authRespond)
                .build();
    }
}
