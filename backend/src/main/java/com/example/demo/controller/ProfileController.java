package com.example.demo.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.DTO.request.ProfileRequest;
import com.example.demo.DTO.response.ApiResponse;
import com.example.demo.DTO.response.ProfileDTO;
import com.example.demo.service.ProfileService;
import com.example.demo.utils.SecurityUtils;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/me")
public class ProfileController {

    @Autowired
    ProfileService profileService;

    @GetMapping("/profile")
    public ApiResponse<ProfileDTO> getMethodName() {
        String currentUsername = SecurityUtils.getCurrentUsername();
        Long accountId = Long.parseLong(currentUsername);
        ProfileDTO profileDTO = profileService.getProfile(accountId);
        return ApiResponse.<ProfileDTO>builder()
                .message("account found")
                .result(profileDTO)
                .build();

    }

    @PutMapping("/update-profile")
    public ApiResponse<ProfileRequest> putMethodName(@RequestBody ProfileRequest profileRequest) {
        String currentUsername = SecurityUtils.getCurrentUsername();
        Long accountId = Long.parseLong(currentUsername);
        ProfileRequest profile = profileService.updateProfile(accountId, profileRequest);
        return ApiResponse.<ProfileRequest>builder()
                .message("Account updated successfull")
                .result(profile)
                .build();
    }

    @PutMapping("/change-password")
    public ApiResponse<String> putMethodName(@RequestBody Map<String, String> payload) {
        String oldPassword = payload.get("oldPassword");
        String newPassword = payload.get("newPassword");
        String currentUsername = SecurityUtils.getCurrentUsername();
        Long accountId = Long.parseLong(currentUsername);

        profileService.changePassword(accountId, oldPassword, newPassword);

        return ApiResponse.<String>builder()
                .message("Password changed successful")
                .result("password changed")
                .build();
    }

    @PostMapping("/request-change-email")
    public ApiResponse<String> postMethodName(@RequestBody Map<String, String> payload) {
        String newEmail = payload.get("newEmail");
        profileService.requestChangeEmail(newEmail);
        return ApiResponse.<String>builder()
                .message("successful")
                .result("Email send")
                .build();
    }

    @PutMapping("/confirm-change-email")
    public ApiResponse<String> confirmChangePassword(@RequestBody Map<String, String> payload) {
        String newEmail = payload.get("newEmail");
        String otp = payload.get("otp");
        String currentUsername = SecurityUtils.getCurrentUsername();
        Long accountId = Long.parseLong(currentUsername);

        profileService.confirmChangeEmail(accountId, otp, newEmail);

        return ApiResponse.<String>builder()
                .message("Email changed successful")
                .result("Email Changed")
                .build();
    }

    @PutMapping("/change-avatar")
    public ApiResponse<String> putMethodName(@PathVariable String id, @RequestBody String entity) {

        return ApiResponse.<String>builder()
                .message("Avatar is changed")
                .result("Avatar changed")
                .build();
    }

}
