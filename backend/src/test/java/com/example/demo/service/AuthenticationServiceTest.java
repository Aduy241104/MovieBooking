package com.example.demo.service;

import com.example.demo.DTO.request.AccountWithOtp;
import com.example.demo.DTO.request.AuthenticationRequest;
import com.example.demo.DTO.request.IntrospectRequest;
import com.example.demo.DTO.request.RegisterRequest;
import com.example.demo.DTO.response.AccountRespond;
import com.example.demo.DTO.response.AuthRespond;
import com.example.demo.DTO.response.IntrospectRespond;
import com.example.demo.exception.EmailAlreadyExistsException;
import com.example.demo.exception.NotFoundException;
import com.example.demo.exception.UnauthorizedException;
import com.example.demo.mapper.AccountMapper;
import com.example.demo.model.Account;
import com.example.demo.model.RefreshToken;
import com.example.demo.model.Role;
import com.example.demo.repository.AccountRepository;
import com.example.demo.repository.RoleRepository;
import com.example.demo.utils.GoogleTokenVerifier;
import com.nimbusds.jose.JOSEException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AuthenticationService
 * Testing core authentication, registration, and JWT operations
 */
@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private AccountMapper accountMapper;

    @Mock
    private OtpService otpService;

    @Mock
    private RefreshTokenService refreshTokenService;

    @Mock
    private GoogleTokenVerifier googleTokenVerifier;

    @InjectMocks
    private AuthenticationService authenticationService;

    private static final String TEST_SIGNER_KEY = "mySecretKeyForJWTSigningThatShouldBeLongEnoughForHS512Algorithm";
    private Account testAccount;
    private Role testRole;
    private AuthenticationRequest authRequest;
    private RegisterRequest registerRequest;
    private AccountWithOtp accountWithOtp;

    @BeforeEach
    void setUp() {
        // Set up signer key using reflection
        ReflectionTestUtils.setField(authenticationService, "SIGNER_KEY", TEST_SIGNER_KEY);

        // Set up test data
        testRole = new Role();
        testRole.setRoleId(1L);
        testRole.setRoleName("CUSTOMER");

        testAccount = Account.builder()
                .accountId(1L)
                .email("test@example.com")
                .fullName("Test User")
                .password("encodedPassword")
                .status(1)
                .isDeleted(false)
                .role(testRole)
                .socialAccountType("LOCAL")
                .build();

        authRequest = AuthenticationRequest.builder()
                .username("test@example.com")
                .password("plainPassword")
                .build();

        registerRequest = RegisterRequest.builder()
                .email("newuser@example.com")
                .fullName("New User")
                .password("plainPassword")
                .build();

        accountWithOtp = AccountWithOtp.builder()
                .registerRequest(registerRequest)
                .verifyOtp("123456")
                .build();
    }

    // ========== AUTH METHOD TESTS ==========

    @Test
    void testAuth_whenValidCredentials_shouldReturnSuccessfulAuthResponse() {
        // Arrange
        RefreshToken mockRefreshToken = RefreshToken.builder()
                .token("refresh-token")
                .account(testAccount)
                .build();

        AccountRespond mockAccountRespond = AccountRespond.builder()
                .accountID(1L)
                .email("test@example.com")
                .fullName("Test User")
                .build();

        when(accountRepository.findByEmailAndStatus("test@example.com", 1))
                .thenReturn(Optional.of(testAccount));
        when(passwordEncoder.matches("plainPassword", "encodedPassword"))
                .thenReturn(true);
        when(refreshTokenService.createRefresToken(testAccount))
                .thenReturn(mockRefreshToken);
        when(accountMapper.toAccountRespond(testAccount))
                .thenReturn(mockAccountRespond);

        // Act
        AuthRespond result = authenticationService.auth(authRequest);

        // Assert
        assertNotNull(result);
        assertTrue(result.isAuthenticated());
        assertNotNull(result.getToken());
        assertEquals("refresh-token", result.getRefresToken());
        assertEquals(mockAccountRespond, result.getAccount());
        verify(accountRepository).findByEmailAndStatus("test@example.com", 1);
        verify(passwordEncoder).matches("plainPassword", "encodedPassword");
        verify(refreshTokenService).createRefresToken(testAccount);
    }

    @Test
    void testAuth_whenUserNotFound_shouldThrowNotFoundException() {
        // Arrange
        when(accountRepository.findByEmailAndStatus("test@example.com", 1))
                .thenReturn(Optional.empty());

        // Act & Assert
        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> authenticationService.auth(authRequest));
        assertEquals("User not found", exception.getMessage());
        verify(accountRepository).findByEmailAndStatus("test@example.com", 1);
        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }

    @Test
    void testAuth_whenInvalidPassword_shouldThrowUnauthorizedException() {
        // Arrange
        when(accountRepository.findByEmailAndStatus("test@example.com", 1))
                .thenReturn(Optional.of(testAccount));
        when(passwordEncoder.matches("plainPassword", "encodedPassword"))
                .thenReturn(false);

        // Act & Assert
        UnauthorizedException exception = assertThrows(UnauthorizedException.class,
                () -> authenticationService.auth(authRequest));
        assertEquals("Invalid Password.", exception.getMessage());
        verify(accountRepository).findByEmailAndStatus("test@example.com", 1);
        verify(passwordEncoder).matches("plainPassword", "encodedPassword");
        verify(refreshTokenService, never()).createRefresToken(any());
    }

    @Test
    void testAuth_whenInactiveAccount_shouldThrowNotFoundException() {
        // Arrange
        when(accountRepository.findByEmailAndStatus("test@example.com", 1))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NotFoundException.class,
                () -> authenticationService.auth(authRequest));
        verify(accountRepository).findByEmailAndStatus("test@example.com", 1);
    }

    // ========== HANDLE SEND OTP TESTS ==========

    @Test
    void testHandleSendOtp_whenEmailNotExists_shouldSendOtpSuccessfully() {
        // Arrange
        when(accountRepository.existsByEmail("newuser@example.com"))
                .thenReturn(false);

        // Act
        assertDoesNotThrow(() -> authenticationService.handleSendOtp(registerRequest));

        // Assert
        verify(accountRepository).existsByEmail("newuser@example.com");
        verify(otpService).sendOtp("newuser@example.com");
    }

    @Test
    void testHandleSendOtp_whenEmailAlreadyExists_shouldThrowEmailAlreadyExistsException() {
        // Arrange
        when(accountRepository.existsByEmail("newuser@example.com"))
                .thenReturn(true);

        // Act & Assert
        EmailAlreadyExistsException exception = assertThrows(EmailAlreadyExistsException.class,
                () -> authenticationService.handleSendOtp(registerRequest));
        assertEquals("Email đã tồn tại", exception.getMessage());
        verify(accountRepository).existsByEmail("newuser@example.com");
        verify(otpService, never()).sendOtp(anyString());
    }

    // ========== CREATE ACCOUNT TESTS ==========

    @Test
    void testCreateAccount_whenValidOtp_shouldCreateAccountSuccessfully() {
        // Arrange
        Account mappedAccount = Account.builder()
                .email("newuser@example.com")
                .fullName("New User")
                .password("plainPassword")
                .role(testRole)
                .build();

        when(otpService.verifyOtp("newuser@example.com", "123456"))
                .thenReturn(true);
        when(roleRepository.findByRoleName("CUSTOMER"))
                .thenReturn(Optional.of(testRole));
        when(accountMapper.toAccount(registerRequest, testRole))
                .thenReturn(mappedAccount);
        when(passwordEncoder.encode("plainPassword"))
                .thenReturn("encodedPassword");
        when(accountRepository.save(any(Account.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Account result = authenticationService.createAccount(accountWithOtp);

        // Assert
        assertNotNull(result);
        assertEquals("newuser@example.com", result.getEmail());
        assertEquals("encodedPassword", result.getPassword());
        assertEquals("LOCAL", result.getSocialAccountType());
        verify(otpService).verifyOtp("newuser@example.com", "123456");
        verify(roleRepository).findByRoleName("CUSTOMER");
        verify(passwordEncoder).encode("plainPassword");
        verify(accountRepository).save(any(Account.class));
        verify(otpService).clearOtp("newuser@example.com");
    }

    @Test
    void testCreateAccount_whenInvalidOtp_shouldThrowUnauthorizedException() {
        // Arrange
        when(otpService.verifyOtp("newuser@example.com", "123456"))
                .thenReturn(false);

        // Act & Assert
        UnauthorizedException exception = assertThrows(UnauthorizedException.class,
                () -> authenticationService.createAccount(accountWithOtp));
        assertEquals("OTP không hợp lệ hoặc đã hết hạn.", exception.getMessage());
        verify(otpService).verifyOtp("newuser@example.com", "123456");
        verify(roleRepository, never()).findByRoleName(anyString());
        verify(accountRepository, never()).save(any());
    }

    @Test
    void testCreateAccount_whenCustomerRoleNotFound_shouldThrowRuntimeException() {
        // Arrange
        when(otpService.verifyOtp("newuser@example.com", "123456"))
                .thenReturn(true);
        when(roleRepository.findByRoleName("CUSTOMER"))
                .thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> authenticationService.createAccount(accountWithOtp));
        assertEquals("Role Customer không tồn tại", exception.getMessage());
        verify(otpService).verifyOtp("newuser@example.com", "123456");
        verify(roleRepository).findByRoleName("CUSTOMER");
        verify(accountRepository, never()).save(any());
    }

    // ========== HANDLE FORGOT PASSWORD TESTS ==========

    @Test
    void testHandleForgotPassword_whenAccountExists_shouldSendResetLink() {
        // Arrange
        when(accountRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(testAccount));

        // Act
        assertDoesNotThrow(() -> authenticationService.handleForgotPassword("test@example.com"));

        // Assert
        verify(accountRepository).findByEmail("test@example.com");
        verify(otpService).sendForgotPasswordLink(testAccount);
    }

    @Test
    void testHandleForgotPassword_whenAccountNotExists_shouldThrowNotFoundException() {
        // Arrange
        when(accountRepository.findByEmail("nonexistent@example.com"))
                .thenReturn(Optional.empty());

        // Act & Assert
        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> authenticationService.handleForgotPassword("nonexistent@example.com"));
        assertEquals("Account does not exists", exception.getMessage());
        verify(accountRepository).findByEmail("nonexistent@example.com");
        verify(otpService, never()).sendForgotPasswordLink(any());
    }

    // ========== HANDLE RESET PASSWORD TESTS ==========

    @Test
    void testHandleResetPassword_whenValidOtp_shouldResetPasswordSuccessfully() {
        // Arrange
        when(otpService.verifyOtp("test@example.com", "123456"))
                .thenReturn(true);
        when(accountRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(testAccount));
        when(passwordEncoder.encode("newPassword"))
                .thenReturn("encodedNewPassword");
        when(accountRepository.save(any(Account.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        assertDoesNotThrow(
                () -> authenticationService.handleResetPassword("test@example.com", "123456", "newPassword"));

        // Assert
        verify(otpService).verifyOtp("test@example.com", "123456");
        verify(accountRepository).findByEmail("test@example.com");
        verify(passwordEncoder).encode("newPassword");
        verify(accountRepository).save(testAccount);
        verify(otpService).clearOtp("test@example.com");
        assertEquals("encodedNewPassword", testAccount.getPassword());
    }

    @Test
    void testHandleResetPassword_whenInvalidOtp_shouldThrowUnauthorizedException() {
        // Arrange
        when(otpService.verifyOtp("test@example.com", "123456"))
                .thenReturn(false);

        // Act & Assert
        UnauthorizedException exception = assertThrows(UnauthorizedException.class,
                () -> authenticationService.handleResetPassword("test@example.com", "123456", "newPassword"));
        assertEquals("Invalid or incorrect OTP.", exception.getMessage());
        verify(otpService).verifyOtp("test@example.com", "123456");
        verify(accountRepository, never()).findByEmail(anyString());
        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    void testHandleResetPassword_whenAccountNotFound_shouldThrowNotFoundException() {
        // Arrange
        when(otpService.verifyOtp("test@example.com", "123456"))
                .thenReturn(true);
        when(accountRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.empty());

        // Act & Assert
        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> authenticationService.handleResetPassword("test@example.com", "123456", "newPassword"));
        assertEquals("account not found", exception.getMessage());
        verify(otpService).verifyOtp("test@example.com", "123456");
        verify(accountRepository).findByEmail("test@example.com");
        verify(passwordEncoder, never()).encode(anyString());
    }

    // ========== GOOGLE LOGIN TESTS ==========

    @Test
    void testHandleLoginWithGoogle_whenValidTokenAndNewUser_shouldCreateAccountAndReturnAuth() {
        // Arrange
        Map<String, Object> googlePayload = new HashMap<>();
        googlePayload.put("email", "google@example.com");
        googlePayload.put("name", "Google User");
        googlePayload.put("picture", "http://example.com/picture.jpg");

        RefreshToken mockRefreshToken = RefreshToken.builder()
                .token("refresh-token")
                .account(testAccount)
                .build();

        AccountRespond mockAccountRespond = AccountRespond.builder()
                .accountID(1L)
                .email("google@example.com")
                .fullName("Google User")
                .build();

        when(googleTokenVerifier.verify("valid-google-token"))
                .thenReturn(googlePayload);
        when(accountRepository.existsByEmail("google@example.com"))
                .thenReturn(false);
        when(roleRepository.findByRoleName("CUSTOMER"))
                .thenReturn(Optional.of(testRole));
        when(accountRepository.save(any(Account.class)))
                .thenAnswer(invocation -> {
                    Account account = invocation.getArgument(0);
                    account.setAccountId(2L);
                    return account;
                });
        when(refreshTokenService.createRefresToken(any(Account.class)))
                .thenReturn(mockRefreshToken);
        when(accountMapper.toAccountRespond(any(Account.class)))
                .thenReturn(mockAccountRespond);

        // Act
        AuthRespond result = authenticationService.handleLoginWithGoogle("valid-google-token");

        // Assert
        assertNotNull(result);
        assertTrue(result.isAuthenticated());
        assertNotNull(result.getToken());
        assertEquals("refresh-token", result.getRefresToken());
        assertEquals(mockAccountRespond, result.getAccount());
        verify(googleTokenVerifier).verify("valid-google-token");
        verify(accountRepository).existsByEmail("google@example.com");
        verify(roleRepository).findByRoleName("CUSTOMER");
        verify(accountRepository).save(any(Account.class));
    }

    @Test
    void testHandleLoginWithGoogle_whenValidTokenAndExistingGoogleUser_shouldReturnAuth() {
        // Arrange
        Map<String, Object> googlePayload = new HashMap<>();
        googlePayload.put("email", "google@example.com");
        googlePayload.put("name", "Google User");
        googlePayload.put("picture", "http://example.com/picture.jpg");

        Account existingGoogleAccount = Account.builder()
                .accountId(2L)
                .email("google@example.com")
                .fullName("Google User")
                .status(1)
                .socialAccountType("GOOGLE")
                .role(testRole)
                .build();

        RefreshToken mockRefreshToken = RefreshToken.builder()
                .token("refresh-token")
                .account(existingGoogleAccount)
                .build();

        AccountRespond mockAccountRespond = AccountRespond.builder()
                .accountID(2L)
                .email("google@example.com")
                .fullName("Google User")
                .build();

        when(googleTokenVerifier.verify("valid-google-token"))
                .thenReturn(googlePayload);
        when(accountRepository.existsByEmail("google@example.com"))
                .thenReturn(true);
        when(accountRepository.findByEmailAndStatus("google@example.com", 1))
                .thenReturn(Optional.of(existingGoogleAccount));
        when(refreshTokenService.createRefresToken(existingGoogleAccount))
                .thenReturn(mockRefreshToken);
        when(accountMapper.toAccountRespond(existingGoogleAccount))
                .thenReturn(mockAccountRespond);

        // Act
        AuthRespond result = authenticationService.handleLoginWithGoogle("valid-google-token");

        // Assert
        assertNotNull(result);
        assertTrue(result.isAuthenticated());
        assertNotNull(result.getToken());
        assertEquals("refresh-token", result.getRefresToken());
        assertEquals(mockAccountRespond, result.getAccount());
        verify(googleTokenVerifier).verify("valid-google-token");
        verify(accountRepository).existsByEmail("google@example.com");
        verify(accountRepository).findByEmailAndStatus("google@example.com", 1);
        verify(accountRepository, never()).save(any());
    }

    @Test
    void testHandleLoginWithGoogle_whenInvalidToken_shouldThrowNotFoundException() {
        // Arrange
        when(googleTokenVerifier.verify("invalid-token"))
                .thenReturn(null);

        // Act & Assert
        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> authenticationService.handleLoginWithGoogle("invalid-token"));
        assertEquals("id not valid", exception.getMessage());
        verify(googleTokenVerifier).verify("invalid-token");
        verify(accountRepository, never()).existsByEmail(anyString());
    }

    @Test
    void testHandleLoginWithGoogle_whenEmailExistsAsLocalAccount_shouldThrowEmailAlreadyExistsException() {
        // Arrange
        Map<String, Object> googlePayload = new HashMap<>();
        googlePayload.put("email", "test@example.com");
        googlePayload.put("name", "Test User");
        googlePayload.put("picture", "http://example.com/picture.jpg");

        Account existingLocalAccount = Account.builder()
                .accountId(1L)
                .email("test@example.com")
                .socialAccountType("LOCAL")
                .build();

        when(googleTokenVerifier.verify("valid-google-token"))
                .thenReturn(googlePayload);
        when(accountRepository.existsByEmail("test@example.com"))
                .thenReturn(true);
        when(accountRepository.findByEmailAndStatus("test@example.com", 1))
                .thenReturn(Optional.of(existingLocalAccount));

        // Act & Assert
        EmailAlreadyExistsException exception = assertThrows(EmailAlreadyExistsException.class,
                () -> authenticationService.handleLoginWithGoogle("valid-google-token"));
        assertEquals("Email này đã được đăng ký bằng email và mật khẩu.", exception.getMessage());
        verify(googleTokenVerifier).verify("valid-google-token");
        verify(accountRepository).existsByEmail("test@example.com");
        verify(accountRepository).findByEmailAndStatus("test@example.com", 1);
    }

    // ========== GENERATE TOKEN TESTS ==========

    @Test
    void testGenerateToken_whenValidAccount_shouldReturnJwtToken() {
        // Arrange
        // (testAccount already set up in setUp method)

        // Act
        String token = authenticationService.generateToken(testAccount);

        // Assert
        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertTrue(token.contains("."));
        // JWT should have 3 parts separated by dots
        assertEquals(3, token.split("\\.").length);
        // Note: For more thorough testing, you might want to parse and verify the JWT
        // claims
    }

    @Test
    void testGenerateToken_whenAccountWithNullRole_shouldHandleGracefully() {
        // Arrange
        Account accountWithNullRole = Account.builder()
                .accountId(1L)
                .email("test@example.com")
                .role(null)
                .build();

        // Act & Assert
        // This might throw an exception depending on your implementation
        assertThrows(NullPointerException.class,
                () -> authenticationService.generateToken(accountWithNullRole));
    }

    // ========== INTROSPECT TESTS ==========

    @Test
    void testIntrospect_whenValidToken_shouldReturnValidResponse() throws JOSEException, ParseException {
        // Arrange
        String validToken = authenticationService.generateToken(testAccount);
        IntrospectRequest introspectRequest = IntrospectRequest.builder()
                .token(validToken)
                .build();

        // Act
        IntrospectRespond result = authenticationService.introspect(introspectRequest);

        // Assert
        assertNotNull(result);
        assertTrue(result.isValid());
    }

    @Test
    void testIntrospect_whenExpiredToken_shouldReturnInvalidResponse() throws JOSEException, ParseException {
        // Arrange - Use a properly formatted JWT token but with wrong signature/expired
        // time
        // This will test the actual logic flow without throwing parsing exceptions
        String expiredToken = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxIiwiaXNzIjoiYW5oZHV5LmNvbSIsImlhdCI6MTY0MDk5NTIwMCwiZXhwIjoxNjQwOTk1MjAxLCJzY29wZSI6IkNVU1RPTUVSIn0.wrongSignature";
        IntrospectRequest introspectRequest = IntrospectRequest.builder()
                .token(expiredToken)
                .build();

        // Act
        IntrospectRespond result = authenticationService.introspect(introspectRequest);

        // Assert - The token should be invalid due to wrong signature
        assertNotNull(result);
        assertFalse(result.isValid());
    }

    @Test
    void testIntrospect_whenMalformedToken_shouldThrowException() {
        // Arrange
        IntrospectRequest introspectRequest = IntrospectRequest.builder()
                .token("malformed.token")
                .build();

        // Act & Assert
        assertThrows(Exception.class, () -> authenticationService.introspect(introspectRequest));
    }

    @Test
    void testIntrospect_whenNullToken_shouldThrowException() {
        // Arrange
        IntrospectRequest introspectRequest = IntrospectRequest.builder()
                .token(null)
                .build();

        // Act & Assert
        assertThrows(Exception.class, () -> authenticationService.introspect(introspectRequest));
    }

    @Test
    void testIntrospect_whenEmptyToken_shouldThrowException() {
        // Arrange
        IntrospectRequest introspectRequest = IntrospectRequest.builder()
                .token("")
                .build();

        // Act & Assert
        assertThrows(Exception.class, () -> authenticationService.introspect(introspectRequest));
    }
}
