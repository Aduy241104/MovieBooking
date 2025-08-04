package com.example.demo.controller;


import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.example.demo.DTO.request.AccountWithOtp;
import com.example.demo.DTO.request.AuthenticationRequest;
import com.example.demo.DTO.request.RegisterRequest;
import com.example.demo.DTO.response.ApiResponse;
import com.example.demo.DTO.response.AuthRespond;
import com.example.demo.service.AuthenticationService;
import com.example.demo.service.OtpService;
import com.example.demo.service.RefreshTokenService;
import com.example.demo.utils.SecurityUtils;

import jakarta.validation.Valid;

import com.example.demo.model.Account;
import com.example.demo.model.RefreshToken;
import com.example.demo.path.AuthenticationPath;
import com.example.demo.repository.RefreshTokenRepository;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/auth")
public class AuthenticationController {

    @Autowired
    AuthenticationService authService;

    @Autowired
    SecurityUtils securityUtils;

    @Autowired
    OtpService otpService;

    @Autowired
    RefreshTokenService refreshTokenService;

    @Autowired
    RefreshTokenRepository refreshTokenRepository;


    @PostMapping(AuthenticationPath.LOGIN_OAUTH)
    public ApiResponse<AuthRespond> loginMethod(@Valid @RequestBody AuthenticationRequest request) {
        AuthRespond authRespond = authService.login(request);
        return ApiResponse.<AuthRespond>builder()
                .message("success")
                .result(authRespond)
                .build();
    }

    @PostMapping(AuthenticationPath.REGISTER)
    public ApiResponse<String> registerMethod(@RequestBody @Valid RegisterRequest request) {
        authService.handleSendOtp(request);
        return ApiResponse.<String>builder()
                .result("Email send")
                .build();
    }

    @PostMapping(AuthenticationPath.VERIFY_OTP)
    public ApiResponse<Account> verifyOtpAndCreatAccount(@RequestBody AccountWithOtp accountWithOtp) {
        Account account = authService.createAccount(accountWithOtp);
        return ApiResponse.<Account>builder()
                .message("sign up success")
                .result(account)
                .build();
    }

    @PostMapping(AuthenticationPath.RESEND_OTP)
    public ApiResponse<String> postMethodResendOtp(@RequestBody RegisterRequest request) {
        authService.handleSendOtp(request);
        return ApiResponse.<String>builder()
                .result("Email send")
                .build();
    }

    @PostMapping(AuthenticationPath.FORGOT_PASSWORD)
    public ApiResponse<String> postMethodName(@RequestBody Map<String, String> payload) {
        String email = payload.get("email");
        authService.handleForgotPassword(email);
        return ApiResponse.<String>builder()
                .message("success")
                .result("email send")
                .build();
    }

    @PostMapping(AuthenticationPath.RESET_PASSWORD)
    public ApiResponse<String> resetPassword(@RequestBody Map<String, String> payload) {
        String otp = payload.get("otp");
        String email = payload.get("email");
        String newPass = payload.get("newPass");

        authService.handleResetPassword(email, otp, newPass);

        return ApiResponse.<String>builder()
                .message("successful")
                .result("password updated")
                .build();
    }

    @PostMapping(AuthenticationPath.REFRESH_TOKEN)
    public ResponseEntity<?> refreshToken(@RequestBody Map<String, String> request) {
        String requestToken = request.get("refreshToken");

        RefreshToken token = refreshTokenRepository.findByToken(requestToken)
                .map(refreshTokenService::verifyExpiration)
                .orElseThrow(() -> new RuntimeException("Invalid refresh token"));

        Account account = token.getAccount();
        String newAccessToken = securityUtils.generateToken(account);
        return ResponseEntity.ok(Map.of("accessToken", newAccessToken));
    }

}