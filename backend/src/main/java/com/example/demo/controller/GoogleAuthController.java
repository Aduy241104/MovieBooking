package com.example.demo.controller;

import java.io.IOException;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.util.UriComponentsBuilder;

import com.example.demo.DTO.response.ApiResponse;
import com.example.demo.DTO.response.AuthRespond;
import com.example.demo.service.AuthenticationService;
import org.springframework.beans.factory.annotation.Value;

import jakarta.servlet.http.HttpServletResponse;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/auth")
public class GoogleAuthController {

    @Value("${google.client-id}")
    private String clientId;

    @Value("${google.redirect-uri}")
    private String redirectUri;

    @Value("${google.client-secret}")
    private String clientSecret;

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

    @GetMapping("/google/google-login")
public void redirectToGoogle(HttpServletResponse response) throws IOException {
    String authUrl = UriComponentsBuilder
            .fromUriString("https://accounts.google.com/o/oauth2/v2/auth")
            .queryParam("client_id", clientId)
            .queryParam("redirect_uri", redirectUri)
            .queryParam("response_type", "code")
            .queryParam("scope",
                    "openid email profile https://www.googleapis.com/auth/user.birthday.read https://www.googleapis.com/auth/user.phonenumbers.read")
            .queryParam("access_type", "offline")
            .build().toUriString();

    response.sendRedirect(authUrl);
}


    private final RestTemplate restTemplate = new RestTemplate();

    @PostMapping("/google/callback")
    public ResponseEntity<?> googleCallback(@RequestBody Map<String, String> bodys) {
        String code = bodys.get("code");
        if (code == null)
            return ResponseEntity.badRequest().body(Map.of("error", "Code is missing"));
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED); // ✅ Fix MediaType

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("client_id", clientId);
        body.add("client_secret", clientSecret);
        body.add("code", code);
        body.add("redirect_uri", redirectUri);
        body.add("grant_type", "authorization_code");

        HttpEntity<MultiValueMap<String, String>> tokenRequest = new HttpEntity<>(body, headers); // ✅ Fix generic

        ResponseEntity<Map<String, Object>> tokenResponse = restTemplate.exchange(
                "https://oauth2.googleapis.com/token",
                HttpMethod.POST,
                tokenRequest,
                new ParameterizedTypeReference<Map<String, Object>>() {
                });

        String accessToken = (String) tokenResponse.getBody().get("access_token");

        // 2. Get user info
        HttpHeaders userInfoHeaders = new HttpHeaders();
        userInfoHeaders.set("Authorization", "Bearer " + accessToken); // ✅ Fix setBearerAuth

        HttpEntity<Void> userInfoRequest = new HttpEntity<>(userInfoHeaders);

        ResponseEntity<Map<String, Object>> userInfoResponse = restTemplate.exchange(
                "https://www.googleapis.com/oauth2/v3/userinfo",
                HttpMethod.GET,
                userInfoRequest,
                new ParameterizedTypeReference<Map<String, Object>>() {
                });

        Map<String, Object> userInfo = userInfoResponse.getBody();

        // String email = (String) userInfo.get("email");
        // String name = (String) userInfo.get("name");
        // String picture = (String) userInfo.get("picture");

        // Map<String, Object> result = new HashMap<>();
        // result.put("email", email);
        // result.put("name", name);
        // result.put("picture", picture);

        return ResponseEntity.ok(userInfo); 
    }
}
