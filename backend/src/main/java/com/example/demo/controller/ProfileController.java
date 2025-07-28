package com.example.demo.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.DTO.request.ChangePasswordRequest;
import com.example.demo.DTO.request.ProfileRequest;
import com.example.demo.DTO.response.AccountRespond;
import com.example.demo.DTO.response.ApiResponse;
import com.example.demo.DTO.response.ProfileDTO;
import com.example.demo.DTO.response.RefreshAndAccessTokenResponse;
import com.example.demo.path.UserProfilePath;
import com.example.demo.service.ProfileService;
import com.example.demo.utils.SecurityUtils;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PostMapping;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/me")
public class ProfileController {

    @Autowired
    ProfileService profileService;

    @GetMapping(UserProfilePath.PROFILE)
    public ApiResponse<ProfileDTO> getMethodName() {
        String currentUsername = SecurityUtils.getCurrentUsername();
        Long accountId = Long.parseLong(currentUsername);
        ProfileDTO profileDTO = profileService.getProfile(accountId);
        return ApiResponse.<ProfileDTO>builder()
                .message("account found")
                .result(profileDTO)
                .build();
    }

    @PutMapping(UserProfilePath.UPDATE_PROFILE)
    public ApiResponse<AccountRespond> putMethodName(@Valid @RequestBody ProfileRequest profileRequest) {
        String currentUsername = SecurityUtils.getCurrentUsername();
        Long accountId = Long.parseLong(currentUsername);
        AccountRespond profile = profileService.updateProfile(accountId, profileRequest);
        return ApiResponse.<AccountRespond>builder()
                .message("Account updated successfull")
                .result(profile)
                .build();
    }

    @PutMapping(UserProfilePath.CHANGE_PASSWORD)
    public ApiResponse<RefreshAndAccessTokenResponse> putMethodName(
            @RequestBody @Valid ChangePasswordRequest changePasswordRequest) {

        String currentUsername = SecurityUtils.getCurrentUsername();
        Long accountId = Long.parseLong(currentUsername);

        RefreshAndAccessTokenResponse newToken = profileService.changePassword(accountId, changePasswordRequest);

        return ApiResponse.<RefreshAndAccessTokenResponse>builder()
                .message("Password changed successful")
                .result(newToken)
                .build();
    }

    @PostMapping(UserProfilePath.REQUEST_CHANGE_EMAIL)
    public ApiResponse<String> postMethodName(@RequestBody Map<String, String> payload) {
        String newEmail = payload.get("newEmail");
        profileService.requestChangeEmail(newEmail);
        return ApiResponse.<String>builder()
                .message("successful")
                .result("Email send")
                .build();
    }

    @PutMapping(UserProfilePath.CONFIRM_CHANGE_EMAIL)
    public ApiResponse<AccountRespond> confirmChangePassword(@RequestBody Map<String, String> payload) {
        String newEmail = payload.get("newEmail");
        String otp = payload.get("otp");
        String currentUsername = SecurityUtils.getCurrentUsername();
        Long accountId = Long.parseLong(currentUsername);

        AccountRespond accountRespond = profileService.confirmChangeEmail(accountId, otp, newEmail);

        return ApiResponse.<AccountRespond>builder()
                .message("Email changed successful")
                .result(accountRespond)
                .build();
    }

    @PutMapping(UserProfilePath.CHANGE_AVATAR)
    public ApiResponse<AccountRespond> changeAvatar(@RequestBody Map<String, String> payload) {
        String avatarUrl = payload.get("avatar");
        String currentUsername = SecurityUtils.getCurrentUsername();
        Long accountId = Long.parseLong(currentUsername);
        AccountRespond accountRespond = profileService.changeAvatar(accountId, avatarUrl);
        return ApiResponse.<AccountRespond>builder()
                .message("Avatar is changed")
                .result(accountRespond)
                .build();
    }
}
