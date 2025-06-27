package com.example.demo.service;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.model.Account;
import com.example.demo.model.RefreshToken;
import com.example.demo.repository.AccountRepository;
import com.example.demo.repository.RefreshTokenRepository;

import jakarta.transaction.Transactional;

@Service
public class RefreshTokenService {

    private final int refreshTokenDurationDays = 7;
    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Transactional
    public RefreshToken createRefresToken(Account account) {

        refreshTokenRepository.deleteByAccount(account);

        RefreshToken refreshToken = RefreshToken.builder()
                .account(account)
                .expiryDate(LocalDateTime.now().plusDays(refreshTokenDurationDays))
                .token(UUID.randomUUID().toString())
                .build();

        return refreshTokenRepository.save(refreshToken);
    }

    public RefreshToken verifyExpiration(RefreshToken token) {
        if (token.getExpiryDate().isBefore(LocalDateTime.now())) {
            refreshTokenRepository.delete(token);
            throw new RuntimeException("Refresh token expired");
        }
        return token;
    }

    public void deleteByAccountId(Long accountId) {
        Account acc = accountRepository.findById(accountId).orElseThrow();
        refreshTokenRepository.deleteByAccount(acc);
    }

}
