package com.example.demo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.DTO.response.ProfileDTO;
import com.example.demo.exception.NotFoundException;
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

    public ProfileDTO getProfile(Long id) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("account not found !"));
        ProfileDTO profileDTO = accountMapper.toPersonalProfile(account);
        return profileDTO;
    }

}
