package com.example.demo.service;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.example.demo.DTO.request.AccountWithOtp;
import com.example.demo.DTO.request.AuthenticationRequest;
import com.example.demo.DTO.request.RegisterRequest;
import com.example.demo.DTO.response.AccountRespond;
import com.example.demo.DTO.response.AuthRespond;
import com.example.demo.enums.RoleTypes;
import com.example.demo.exception.EmailAlreadyExistsException;
import com.example.demo.exception.NotFoundException;
import com.example.demo.exception.RoleNotFoundException;
import com.example.demo.exception.UnauthorizedException;
import com.example.demo.mapper.AccountMapper;
import com.example.demo.model.Account;
import com.example.demo.model.RefreshToken;
import com.example.demo.model.Role;
import com.example.demo.repository.AccountRepository;
import com.example.demo.repository.RoleRepository;
import com.example.demo.utils.GoogleTokenVerifier;
import com.example.demo.utils.SecurityUtils;


import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AuthenticationService {

    @Value("${jwt.signer-key}")
    private String SIGNER_KEY;

    @Autowired
    AccountRepository accountRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    AccountMapper accountMapper;

    @Autowired
    OtpService otpService;

    @Autowired
    RefreshTokenService refreshTokenService;

    @Autowired
    GoogleTokenVerifier googleTokenVerifier;

    @Autowired
    SecurityUtils securityUtils;

    // authentication cho table account
    public AuthRespond auth(AuthenticationRequest request) {
        Account account = accountRepository.findByEmailAndStatus(request.getUsername(), 1)
                .orElseThrow(() -> new NotFoundException("User not found"));

        String raw = request.getPassword();
        boolean match = passwordEncoder.matches(raw, account.getPassword());

        if (!match) {
            throw new UnauthorizedException("Invalid Password.");
        }
        var token = securityUtils.generateToken(account);
        RefreshToken refreshToken = refreshTokenService.createRefresToken(account);

        AccountRespond accountRespond = accountMapper.toAccountRespond(account);
        return AuthRespond.builder()
                .authenticated(true)
                .account(accountRespond)
                .token(token)
                .refresToken(refreshToken.getToken())
                .build();
    }

    public void handleSendOtp(RegisterRequest registerRequest) {
        if (accountRepository.existsByEmail(registerRequest.getEmail())) {
            throw new EmailAlreadyExistsException("Email already exists");
        }
        otpService.sendOtp(registerRequest.getEmail());
    }

    public Account createAccount(AccountWithOtp accountWithOtp) {
        // Kiểm tra OTP
        boolean isOtpValid = otpService.verifyOtp(accountWithOtp.getRegisterRequest().getEmail(),
                accountWithOtp.getVerifyOtp());

        if (!isOtpValid) {
            throw new UnauthorizedException("OTP is invalid or expired");
        }

        RegisterRequest registerRequest = accountWithOtp.getRegisterRequest();

        Role defaultRole = roleRepository.findByRoleName(RoleTypes.CUSTOMER.name())
                .orElseThrow(() -> new RoleNotFoundException("Role Customer does not exist"));

        Account account = accountMapper.toAccount(registerRequest, defaultRole);
        account.setSocialAccountType("LOCAL");
        account.setPassword(passwordEncoder.encode(account.getPassword()));

        accountRepository.save(account);
        otpService.clearOtp(accountWithOtp.getRegisterRequest().getEmail());
        return account;
    }

    public void handleForgotPassword(String email) {
        Account account = accountRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("Account does not exists"));
        otpService.sendForgotPasswordLink(account);
    }

    @Transactional
    public void handleResetPassword(String email, String otp, String newPass) {
        boolean isOtpValid = otpService.verifyOtp(email, otp);

        if (!isOtpValid) {
            throw new UnauthorizedException("OTP is invalid or expired");
        }
        Account account = accountRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("account not found"));

        account.setPassword(passwordEncoder.encode(newPass));
        accountRepository.save(account);
        otpService.clearOtp(email);
    }

    public AuthRespond handleLoginWithGoogle(String idToken) {

        Map<String, Object> payload = googleTokenVerifier.verify(idToken);

        if (payload == null) {
            throw new NotFoundException("id not valid");
        }

        String email = (String) payload.get("email");
        String name = (String) payload.get("name");
        String picture = (String) payload.get("picture");

        boolean checkExists = accountRepository.existsByEmail(email);
        Account account;

        if (checkExists) {
            account = accountRepository.findByEmailAndStatus(email, 1)
                    .orElseThrow(() -> new NotFoundException("User not found"));
            if (account.getSocialAccountType().equals("LOCAL")) {
                throw new EmailAlreadyExistsException("Email này đã được đăng ký bằng email và mật khẩu.");
            }

        } else {
            Role defaultRole = roleRepository.findByRoleName(RoleTypes.CUSTOMER.name())
                    .orElseThrow(() -> new RoleNotFoundException("Role Customer does not exist"));
            account = Account.builder()
                    .email(email)
                    .fullName(name)
                    .avatar(picture)
                    .role(defaultRole)
                    .status(1)
                    .isDeleted(false)
                    .socialAccountType("GOOGLE")
                    .build();

            accountRepository.save(account);
        }

        var token = securityUtils.generateToken(account);
        RefreshToken refreshToken = refreshTokenService.createRefresToken(account);

        AccountRespond accountRespond = accountMapper.toAccountRespond(account);
        return AuthRespond.builder()
                .authenticated(true)
                .account(accountRespond)
                .token(token)
                .refresToken(refreshToken.getToken())
                .build();
    }
}
