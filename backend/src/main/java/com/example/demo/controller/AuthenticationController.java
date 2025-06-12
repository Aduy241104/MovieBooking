package com.example.demo.controller;

import java.text.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.example.demo.DTO.request.AccountWithOtp;
import com.example.demo.DTO.request.AuthenticationRequest;
import com.example.demo.DTO.request.IntrospectRequest;
import com.example.demo.DTO.request.RegisterRequest;
import com.example.demo.DTO.response.ApiResponse;
import com.example.demo.DTO.response.AuthRespond;
import com.example.demo.DTO.response.IntrospectRespond;
import com.example.demo.service.AuthenticationService;
import com.example.demo.service.OtpService;
import com.example.demo.model.Account;
import com.nimbusds.jose.JOSEException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/auth")
public class AuthenticationController {

    @Autowired
    AuthenticationService authService;

    @Autowired
    OtpService otpService;

    @PostMapping("/introspect")
    public ApiResponse<IntrospectRespond> postMethodName(@RequestBody IntrospectRequest request)
            throws JOSEException, ParseException {
        var response = authService.introspect(request);
        return ApiResponse.<IntrospectRespond>builder()
                .message("success")
                .result(response)
                .build();
    }

    @PostMapping("/login-oauth")
    public ApiResponse<AuthRespond> loginMethod(@RequestBody AuthenticationRequest request) {
        AuthRespond authRespond = authService.auth(request);
        return ApiResponse.<AuthRespond>builder()
                .message("success")
                .result(authRespond)
                .build();
    }

    @PostMapping("/register-2")
    public ApiResponse<String> postMethodNames(@RequestBody RegisterRequest request) {
        authService.handleSendOtp(request);
        return ApiResponse.<String>builder()
                .result("Email send")
                .build();
    }

    @PostMapping("/verify-otp")
    public ApiResponse<Account> verifyOtpAndCreatAccount(@RequestBody AccountWithOtp accountWithOtp) {
        Account account = authService.createAccount(accountWithOtp);
        return ApiResponse.<Account>builder()
                .message("sign up success")
                .result(account)
                .build();
    }

    @PostMapping("/resendOtp")
    public ApiResponse<String> postMethodResendOtp(@RequestBody RegisterRequest request) {
        authService.handleSendOtp(request);
        return ApiResponse.<String>builder()
                .result("Email send")
                .build();
    }
}