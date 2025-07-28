package com.example.demo.service;

import com.example.demo.DTO.request.AccountWithOtp;
import com.example.demo.DTO.request.AuthenticationRequest;
import com.example.demo.DTO.request.RegisterRequest;
import com.example.demo.DTO.response.AccountRespond;
import com.example.demo.DTO.response.AuthRespond;
import com.example.demo.enums.RoleTypes;
import com.example.demo.model.Account;
import com.example.demo.model.RefreshToken;
import com.example.demo.model.Role;
import com.example.demo.exception.BadRequestException;
import com.example.demo.exception.EmailAlreadyExistsException;
import com.example.demo.exception.NotFoundException;
import com.example.demo.exception.UnauthorizedException;
import com.example.demo.mapper.AccountMapper;
import com.example.demo.repository.AccountRepository;
import com.example.demo.repository.RoleRepository;
import com.example.demo.utils.SecurityUtils;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class AuthTest {

    @InjectMocks
    private AuthenticationService authService;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private SecurityUtils securityUtils;

    @Mock
    private OtpService otpService;

    @Mock
    private RefreshTokenService refreshTokenService;

    @Mock
    private AccountMapper accountMapper;

    @Test
    void testAuth_Success() {
        // Arrange
        String email = "test@example.com";
        String rawPassword = "password123";
        String encodedPassword = "encodedPassword";
        String token = "mockJwtToken";
        String refreshTokenStr = "mockRefreshToken";

        AuthenticationRequest request = new AuthenticationRequest(email, rawPassword);

        Account account = Account.builder()
                .email(email)
                .password(encodedPassword)
                .build();

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken(refreshTokenStr);

        AccountRespond accountRespond = AccountRespond.builder()
                .email(email)
                .fullName("Test User")
                .build();

        when(accountRepository.findByEmailAndStatus(email, 1)).thenReturn(Optional.of(account));
        when(passwordEncoder.matches(rawPassword, encodedPassword)).thenReturn(true);
        when(securityUtils.generateToken(account)).thenReturn(token);
        when(refreshTokenService.createRefresToken(account)).thenReturn(refreshToken);
        when(accountMapper.toAccountRespond(account)).thenReturn(accountRespond);

        // Act
        AuthRespond response = authService.auth(request);

        // Assert
        assertNotNull(response);
        assertTrue(response.isAuthenticated());
        assertEquals(email, response.getAccount().getEmail());
        assertEquals(token, response.getToken());
        assertEquals(refreshTokenStr, response.getRefresToken());
    }

    @Test
    void testAccountNotFound() {
        String email = "test@example.com";
        String rawPassword = "password123";

        AuthenticationRequest authenticationRequest = new AuthenticationRequest(email, rawPassword);

        when(accountRepository.findByEmailAndStatus(email, 1)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> authService.auth(authenticationRequest));

    }

    @Test
    void testAuth_Invalid_Password() {
        String email = "test@example.com";
        String rawPassword = "rawPass";
        String encodedPassword = "encodedPassword";

        AuthenticationRequest authenticationRequest = new AuthenticationRequest(email, rawPassword);

        Account account = Account.builder()
                .email(email)
                .password(encodedPassword)
                .build();

        when(accountRepository.findByEmailAndStatus(email, 1)).thenReturn(Optional.of(account));
        when(passwordEncoder.matches(rawPassword, encodedPassword)).thenReturn(false);

        assertThrows(UnauthorizedException.class, () -> authService.auth(authenticationRequest));

    }

    @Test
    void handleSendOtp_WhenEmaiExists() {
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setEmail("test@gmail.com");

        when(accountRepository.existsByEmail(registerRequest.getEmail())).thenReturn(true);

        assertThrows(EmailAlreadyExistsException.class, () -> authService.handleSendOtp(registerRequest));
    }

    @Test
    void handleSendOtp_WhenEmaiNotExists() {
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setEmail("test@gmail.com");

        when(accountRepository.existsByEmail(registerRequest.getEmail())).thenReturn(false);

        authService.handleSendOtp(registerRequest);

        verify(otpService).sendOtp("test@gmail.com");
    }

    @Test
    void createAccount_ShouldCreateAccount_WhenOtpValid() {
        // Arrange
        RegisterRequest request = new RegisterRequest();
        request.setEmail("new@example.com");
        request.setPassword("123456");

        AccountWithOtp accountWithOtp = new AccountWithOtp();
        accountWithOtp.setRegisterRequest(request);
        accountWithOtp.setVerifyOtp("123456");

        Role role = new Role(1L, RoleTypes.CUSTOMER.name());

        when(otpService.verifyOtp("new@example.com", "123456")).thenReturn(true);
        when(roleRepository.findByRoleName("CUSTOMER")).thenReturn(Optional.of(role));

        Account mappedAccount = new Account();
        mappedAccount.setEmail("new@example.com");
        mappedAccount.setPassword("123456");

        when(accountMapper.toAccount(request, role)).thenReturn(mappedAccount);
        when(passwordEncoder.encode("123456")).thenReturn("encoded123");

        // Act
        Account savedAccount = authService.createAccount(accountWithOtp);

        // Assert
        assertEquals("new@example.com", savedAccount.getEmail());
        assertEquals("encoded123", savedAccount.getPassword());
        verify(accountRepository).save(any(Account.class));
        verify(otpService).clearOtp("new@example.com");
    }

    @Test
    void createAccount_ShouldThrowException_WhenOtpInvalid() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("user@example.com");

        AccountWithOtp accountWithOtp = new AccountWithOtp();
        accountWithOtp.setRegisterRequest(request);
        accountWithOtp.setVerifyOtp("wrongOtp");

        when(otpService.verifyOtp("user@example.com", "wrongOtp")).thenReturn(false);

        assertThrows(BadRequestException.class, () -> authService.createAccount(accountWithOtp));
    }

    @Test
    void handleForgotPassword_ShouldSendLink_WhenEmailExists() {
        // Arrange
        String email = "user@example.com";
        Account account = new Account();
        account.setEmail(email);

        when(accountRepository.findByEmail(email)).thenReturn(Optional.of(account));

        // Act
        authService.handleForgotPassword(email);

        // Assert
        verify(otpService).sendForgotPasswordLink(account);
    }

    @Test
    void handleForgotPassword_ShouldThrow_WhenEmailNotFound() {
        String email = "notfound@example.com";

        when(accountRepository.findByEmail(email)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> authService.handleForgotPassword(email));
    }

    @Test
    void handleResetPassword_ShouldResetPassword_WhenOtpValid() {
        // Arrange
        String email = "user@example.com";
        String otp = "123456";
        String newPass = "newPassword";

        Account account = new Account();
        account.setEmail(email);
        account.setPassword("oldPassword");

        when(otpService.verifyOtp(email, otp)).thenReturn(true);
        when(accountRepository.findByEmail(email)).thenReturn(Optional.of(account));
        when(passwordEncoder.encode(newPass)).thenReturn("encodedNewPass");

        // Act
        authService.handleResetPassword(email, otp, newPass);

        // Assert
        assertEquals("encodedNewPass", account.getPassword());
        verify(accountRepository).save(account);
        verify(otpService).clearOtp(email);
    }

    @Test
    void handleResetPassword_ShouldThrow_WhenOtpInvalid() {
        String email = "user@example.com";
        String otp = "invalid";
        String newPass = "abc";

        when(otpService.verifyOtp(email, otp)).thenReturn(false);

        assertThrows(BadRequestException.class, () -> authService.handleResetPassword(email, otp, newPass));
    }

    @Test
    void handleResetPassword_ShouldThrow_WhenAccountNotFound() {
        String email = "notfound@example.com";
        String otp = "123456";
        String newPass = "pass";

        when(otpService.verifyOtp(email, otp)).thenReturn(true);
        when(accountRepository.findByEmail(email)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> authService.handleResetPassword(email, otp, newPass));
    }
}
