package com.example.demo.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import com.example.demo.DTO.request.RegisterRequest;
import com.example.demo.DTO.response.AccountRespond;
import com.example.demo.model.Account;
import com.example.demo.model.Role;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
public class AccountMapper {

    public Account toAccount(RegisterRequest registerRequest, Role defaultRole) {
        return Account.builder()
                .email(registerRequest.getEmail())
                .password(registerRequest.getPassword())
                .fullName(registerRequest.getFullName())
                .gender(registerRequest.getGender())
                .phoneNumber(registerRequest.getPhoneNumber())
                .identityCard(null)
                .dateOfBirth(registerRequest.getDateOfBirth())
                .registerDate(LocalDate.now())
                .score(0)
                .status(1)
                .socialAccountType(null)
                .role(defaultRole)
                .build();
    }

    public AccountRespond toAccountRespond(Account account) {
        return AccountRespond.builder()
                .accountID(account.getAccountId())
                .email(account.getEmail())
                .fullName(account.getFullName())
                .gender(account.getGender())
                .phoneNumber(account.getPhoneNumber())
                .identityCard(account.getIdentityCard())
                .dateOfBirth(account.getDateOfBirth())
                .registerDate(account.getRegisterDate())
                .score(account.getScore())
                .role(account.getRole().getRoleName())
                .build();
    }

}
