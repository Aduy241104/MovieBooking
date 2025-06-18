package com.example.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.DTO.response.ApiResponse;
import com.example.demo.DTO.response.ProfileDTO;
import com.example.demo.service.ProfileService;
import com.example.demo.utils.SecurityUtils;

import org.springframework.web.bind.annotation.GetMapping;

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
}
