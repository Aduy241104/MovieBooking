package com.example.demo.service;

import java.time.LocalDateTime;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

// import com.example.demo.DTO.request.OtpTokenDTO;
// import com.example.demo.model.Account;

import com.example.demo.model.EmailVerificationToken;
// import com.example.demo.repository.AccountRepository;
import com.example.demo.repository.TokenRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OtpService {

   

    @Autowired
    private EmailService emailService;

    @Autowired
    TokenRepository tokenRepository;

    // @Autowired
    // private AccountRepository accountRepository;

    // generate random otp
    public String generateOtp() {
        return String.valueOf(new Random().nextInt(900000) + 100000); // 6 số
    }

    //
    // public void createSendOtp(Account account) {
    //     String otp = generateOtp();
    //     LocalDateTime expiry = LocalDateTime.now().plusMinutes(5);

    //     AccountTokens accountTokens = AccountTokens.builder()
    //             .account(account)
    //             .emailVerificationToken(otp)
    //             .emailVerificationTokenExpiry(expiry)
    //             .build();
    //     accountTokensRepository.save(accountTokens);
    //     emailService.sendOtpEmail(account.getEmail(), otp);
    // }

    // send otp cho bang moi
    public void sendOtp(String email) {
        String otp = generateOtp();
        LocalDateTime expirationTime = LocalDateTime.now().plusMinutes(5);
        EmailVerificationToken emailVerificationToken = EmailVerificationToken.builder()
                .email(email)
                .otpCode(otp)
                .expirationTime(expirationTime)
                .build();
        tokenRepository.save(emailVerificationToken);
        emailService.sendOtpEmail(email, otp);
    }

    // public void resendOtp(OtpTokenDTO request) {
    //     if (!accountTokensRepository.existsByAccount_AccountId(request.getAccountID())) {
    //         throw new RuntimeException("cannot resolve");
    //     }
    //     accountTokensRepository.deleteById(request.getAccountID());
    //     Account account = accountRepository.findById(request.getAccountID())
    //             .orElseThrow(() -> new RuntimeException("Account not found"));
    //     createSendOtp(account);
    // }

    public boolean verifyOtp(String email, String otp) {
        EmailVerificationToken emailVerificationToken = tokenRepository.findFirstByEmailOrderByExpirationTimeDesc(email)
                .orElse(null);
        if (emailVerificationToken == null) {
            return false;
        }
        boolean isMatch = emailVerificationToken.getOtpCode().equals(otp);
        boolean isExpired = emailVerificationToken.getExpirationTime().isAfter(LocalDateTime.now());
        return isMatch && isExpired;
    }

    @Transactional
    public void clearOtp(String email) {
        tokenRepository.deleteByEmail(email);
    }
}
