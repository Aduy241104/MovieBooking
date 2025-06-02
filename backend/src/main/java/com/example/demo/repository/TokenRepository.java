package com.example.demo.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.model.EmailVerificationToken;

public interface TokenRepository extends JpaRepository<EmailVerificationToken, Long> {
    void deleteByEmail(String email);

    Optional<EmailVerificationToken> findFirstByEmailOrderByExpirationTimeDesc(String email);

}