package com.example.demo.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.example.demo.DTO.request.ChangePasswordRequest;
import com.example.demo.DTO.request.ProfileRequest;
import com.example.demo.DTO.response.AccountRespond;
import com.example.demo.DTO.response.ProfileDTO;
import com.example.demo.exception.NotFoundException;
import com.example.demo.exception.UnauthorizedException;
import com.example.demo.mapper.AccountMapper;
import com.example.demo.model.Account;
import com.example.demo.repository.AccountRepository;
import com.example.demo.utils.SecurityUtils;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class ProfileServiceTest {
    @InjectMocks
    private ProfileService accountService;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private AccountMapper accountMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private SecurityUtils securityUtils;

    @Test
    void getProfile_ShouldReturnProfile_WhenAccountExists() {
        // Arrange
        Long id = 1L;
        Account account = new Account();
        ProfileDTO profileDTO = new ProfileDTO();

        when(accountRepository.findById(id)).thenReturn(Optional.of(account));
        when(accountMapper.toPersonalProfile(account)).thenReturn(profileDTO);

        // Act
        ProfileDTO result = accountService.getProfile(id);

        // Assert
        assertEquals(profileDTO, result);
    }

    @Test
    void getProfile_ShouldThrowNotFound_WhenAccountMissing() {
        Long id = 999L;
        when(accountRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> accountService.getProfile(id));
    }

   @ Test
    void updateProfile_ShouldUpdateAndReturnResponse_WhenAccountExists() {
        // Arrange
        Long id = 1L;
        ProfileRequest request = ProfileRequest.builder()
                .fullName("John Doe")
                .gender("Male")
                .phoneNumber("123456789")
                .dateOfBirth(LocalDate.of(2000, 1, 1))
                .build();

        Account account = new Account();
        AccountRespond accountRespond = new AccountRespond();

        when(accountRepository.findById(id)).thenReturn(Optional.of(account));
        when(accountMapper.toAccountRespond(account)).thenReturn(accountRespond);

        // Act
        AccountRespond result = accountService.updateProfile(id, request);

        // Assert
        assertEquals(accountRespond, result);
        verify(accountRepository).save(account);
        assertEquals("John Doe", account.getFullName());
        assertEquals("Male", account.getGender());
        assertEquals("123456789", account.getPhoneNumber());
        assertEquals(LocalDate.of(2000, 1, 1), account.getDateOfBirth());
    }

    @Test
    void updateProfile_ShouldThrowNotFound_WhenAccountMissing() {
        Long id = 999L;
        ProfileRequest request = new ProfileRequest();

        when(accountRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> accountService.updateProfile(id, request));
    }

    // @Test
    // void changePassword_ShouldUpdatePasswordAndReturnNewToken_WhenOldPasswordValid() {
    //     // Arrange
    //     Long id = 1L;
    //     String oldPass = "old123";
    //     String newPass = "new123";

    //     Account account = new Account();
    //     account.setPassword("encodedOldPass");

    //     ChangePasswordRequest request = new ChangePasswordRequest();
    //     request.setOldPassword(oldPass);
    //     request.setNewPassword(newPass);

    //     when(accountRepository.findById(id)).thenReturn(Optional.of(account));
    //     when(passwordEncoder.matches(oldPass, "encodedOldPass")).thenReturn(true);
    //     when(passwordEncoder.encode(newPass)).thenReturn("encodedNewPass");
    //     when(securityUtils.generateToken(account)).thenReturn("jwt-token");

    //     // Act
    //     String result = accountService.changePassword(id, request);

    //     // Assert
    //     assertEquals("jwt-token", result);
    //     assertEquals("encodedNewPass", account.getPassword());
    //     verify(accountRepository).save(account);
    // }

    @Test
    void changePassword_ShouldThrowUnauthorized_WhenOldPasswordWrong() {
        Long id = 1L;
        Account account = new Account();
        account.setPassword("encodedOld");

        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setOldPassword("wrong");
        request.setNewPassword("new");

        when(accountRepository.findById(id)).thenReturn(Optional.of(account));
        when(passwordEncoder.matches("wrong", "encodedOld")).thenReturn(false);

        assertThrows(UnauthorizedException.class, () -> accountService.changePassword(id, request));
    }

    @Test
    void changePassword_ShouldThrowNotFound_WhenAccountMissing() {
        Long id = 999L;
        ChangePasswordRequest request = new ChangePasswordRequest();

        when(accountRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> accountService.changePassword(id, request));
    }

}
