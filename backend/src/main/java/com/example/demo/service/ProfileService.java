package com.example.demo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.demo.DTO.request.ProfileRequest;
import com.example.demo.DTO.response.AccountRespond;
import com.example.demo.DTO.response.ProfileDTO;
import com.example.demo.exception.EmailAlreadyExistsException;
import com.example.demo.exception.NotFoundException;
import com.example.demo.exception.UnauthorizedException;
import com.example.demo.mapper.AccountMapper;
import com.example.demo.model.Account;
import com.example.demo.repository.AccountRepository;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProfileService {

    @Autowired
    AccountRepository accountRepository;

    @Autowired
    AccountMapper accountMapper;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    OtpService otpService;

    @Autowired
    AuthenticationService authService;

    public ProfileDTO getProfile(Long id) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("account not found !"));
        ProfileDTO profileDTO = accountMapper.toPersonalProfile(account);
        return profileDTO;
    }

    public AccountRespond updateProfile(Long id, ProfileRequest profileRequest) {

        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Account not found"));

        account.setFullName(profileRequest.getFullName());
        account.setGender(profileRequest.getGender());
        account.setPhoneNumber(profileRequest.getPhoneNumber());
        // account.setIdentityCard(profileRequest.getIdentityCard());
        account.setDateOfBirth(profileRequest.getDateOfBirth());
        // account.setAvatar(profileRequest.getAvatar());

        accountRepository.save(account);
        AccountRespond accountRespond = accountMapper.toAccountRespond(account);
        return accountRespond;
    }

    public String changePassword(Long accountId, String oldPassword, String newPassword) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new NotFoundException("Account not found"));

        if (!passwordEncoder.matches(oldPassword, account.getPassword())) {
            throw new UnauthorizedException("Old password is not correct");
        }
        account.setPassword(passwordEncoder.encode(newPassword));
        accountRepository.save(account);
        String newToken = authService.generateToken(account);
        return newToken;

    }

    public void requestChangeEmail(String email) {
        if (accountRepository.existsByEmail(email)) {
            throw new EmailAlreadyExistsException("This Email already exists!");
        }
        otpService.sendOtp(email);
    }

    public AccountRespond confirmChangeEmail(Long accountId, String otp, String newEmail) {
        boolean isValid = otpService.verifyOtp(newEmail, otp);

        if (!isValid) {
            throw new UnauthorizedException("Otp or email not correct");
        }

        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new NotFoundException("Account not found!"));

        account.setEmail(newEmail);
        otpService.clearOtp(newEmail);
        accountRepository.save(account);
        AccountRespond accountRespond = accountMapper.toAccountRespond(account);
        return accountRespond;
    }

    public AccountRespond changeAvatar(Long accountId, String url) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new NotFoundException("Account not found!"));

        account.setAvatar(url);
        accountRepository.save(account);
        AccountRespond accountRespond = accountMapper.toAccountRespond(account);
        return accountRespond;
    }

}
