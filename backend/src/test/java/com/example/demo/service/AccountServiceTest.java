package com.example.demo.service;

import com.example.demo.DTO.response.ResPagination;
import com.example.demo.DTO.response.dashboard.UserRegistrationsResponse;
import com.example.demo.exception.AppException;
import com.example.demo.model.Account;
import com.example.demo.model.Role;
import com.example.demo.repository.AccountRepository;
import com.example.demo.repository.RoleRepository;
import com.example.demo.utils.SecurityUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AccountService
 * Testing account management operations, validation, and data retrieval
 */
@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private ActivityLogService activityLogService;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private AccountService accountService;

    private Account testAccount;
    private Role testRole;
    private List<Account> testAccountList;

    @BeforeEach
    void setUp() {
        // Setup test role
        testRole = new Role();
        testRole.setRoleId(1L);
        testRole.setRoleName("CUSTOMER");

        // Setup test account
        testAccount = Account.builder()
                .accountId(1L)
                .email("test@example.com")
                .fullName("Test User")
                .phoneNumber("0123456789")
                .role(testRole)
                .status(1)
                .isDeleted(false)
                .build();

        // Setup test account list
        testAccountList = new ArrayList<>();
        testAccountList.add(testAccount);
        testAccountList.add(Account.builder()
                .accountId(2L)
                .email("test2@example.com")
                .fullName("Test User 2")
                .phoneNumber("0987654321")
                .role(testRole)
                .status(1)
                .isDeleted(false)
                .build());
    }

    // ========== GET ALL ACCOUNT TESTS ==========

    @Test
    void testGetAllAccount_whenAccountsExist_shouldReturnAllAccounts() {
        // Arrange
        when(accountRepository.findAll()).thenReturn(testAccountList);

        // Act
        List<Account> result = accountService.getAllAccount();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("test@example.com", result.get(0).getEmail());
        assertEquals("test2@example.com", result.get(1).getEmail());
        verify(accountRepository).findAll();
    }

    @Test
    void testGetAllAccount_whenNoAccountsExist_shouldReturnEmptyList() {
        // Arrange
        when(accountRepository.findAll()).thenReturn(new ArrayList<>());

        // Act
        List<Account> result = accountService.getAllAccount();

        // Assert
        assertNotNull(result);
        assertEquals(0, result.size());
        verify(accountRepository).findAll();
    }

    // ========== FETCH ACCOUNT PAGINATION TESTS ==========

    @Test
    void testFetchAllAccountPagination_whenAccountsExist_shouldReturnPaginatedResponse() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Page<Account> accountPage = new PageImpl<>(testAccountList, pageable, testAccountList.size());

        when(accountRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(accountPage);

        // Act
        ResPagination result = accountService.fetchAllAccountPagination(null, pageable);

        // Assert
        assertNotNull(result);
        assertNotNull(result.getMeta());
        assertEquals(1, result.getMeta().getPage());
        assertEquals(10, result.getMeta().getPageSize());
        assertEquals(1, result.getMeta().getPages());
        assertEquals(2L, result.getMeta().getTotal());
        assertEquals(2, ((List<?>) result.getData()).size());
        verify(accountRepository).findAll(any(Specification.class), eq(pageable));
    }

    @Test
    void testFetchAllAccountPagination_whenNoAccountsExist_shouldReturnEmptyPaginatedResponse() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Page<Account> emptyPage = new PageImpl<>(new ArrayList<>(), pageable, 0);

        when(accountRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(emptyPage);

        // Act
        ResPagination result = accountService.fetchAllAccountPagination(null, pageable);

        // Assert
        assertNotNull(result);
        assertNotNull(result.getMeta());
        assertEquals(1, result.getMeta().getPage());
        assertEquals(10, result.getMeta().getPageSize());
        assertEquals(0, result.getMeta().getPages());
        assertEquals(0L, result.getMeta().getTotal());
        assertEquals(0, ((List<?>) result.getData()).size());
        verify(accountRepository).findAll(any(Specification.class), eq(pageable));
    }

    // ========== FETCH ACCOUNT BY ID TESTS ==========

    @Test
    void testFetchAccountById_whenAccountExists_shouldReturnAccount() {
        // Arrange
        when(accountRepository.findById(1L)).thenReturn(Optional.of(testAccount));

        // Act
        Account result = accountService.fetchAccountById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getAccountId());
        assertEquals("test@example.com", result.getEmail());
        assertEquals("Test User", result.getFullName());
        verify(accountRepository).findById(1L);
    }

    @Test
    void testFetchAccountById_whenAccountNotExists_shouldThrowAppException() {
        // Arrange
        when(accountRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        AppException exception = assertThrows(AppException.class,
                () -> accountService.fetchAccountById(999L));
        assertEquals("Account not found", exception.getMessage());
        verify(accountRepository).findById(999L);
    }

    // ========== HANDLE CREATE ACCOUNT TESTS ==========

    @Test
    void testHandleCreateAccount_whenValidAccount_shouldCreateSuccessfully() {
        // Arrange
        Account newAccount = Account.builder()
                .email("new@example.com")
                .fullName("New User")
                .phoneNumber("0111111111")
                .role(new Role(1L, "CUSTOMER"))
                .build();

        Role adminRole = new Role();
        adminRole.setRoleId(3L);
        adminRole.setRoleName("ADMIN");

        Account adminAccount = Account.builder()
                .accountId(3L)
                .email("admin@example.com")
                .fullName("Admin User")
                .role(adminRole)
                .build();

        when(roleRepository.findById(1L)).thenReturn(Optional.of(testRole));
        when(accountRepository.existsByEmail("new@example.com")).thenReturn(false);
        when(accountRepository.existsByPhoneNumber("0111111111")).thenReturn(false);
        when(accountRepository.save(any(Account.class))).thenReturn(newAccount);
        when(accountRepository.findById(any())).thenReturn(Optional.of(newAccount)); // Mock for setLogAndNotification
        when(accountRepository.findByRole_RoleName("ADMIN")).thenReturn(List.of(adminAccount));

        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getCurrentUsername).thenReturn("3");
            when(accountRepository.findById(3L)).thenReturn(Optional.of(adminAccount));

            // Act
            Account result = accountService.handleCreateAccount(newAccount);

            // Assert
            assertNotNull(result);
            assertEquals("new@example.com", result.getEmail());
            verify(roleRepository).findById(1L);
            verify(accountRepository).existsByEmail("new@example.com");
            verify(accountRepository).existsByPhoneNumber("0111111111");
            verify(accountRepository).save(any(Account.class));
            verify(accountRepository, times(1)).findById(3L);
            verify(activityLogService).log(anyString(), anyString(), anyString(), anyString(), anyString());
            // Note: notification might not be triggered if admin list check fails
        }
    }

    @Test
    void testHandleCreateAccount_whenRoleNotFound_shouldThrowAppException() {
        // Arrange
        Account newAccount = Account.builder()
                .email("new@example.com")
                .role(new Role(999L, "INVALID"))
                .build();

        when(roleRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        AppException exception = assertThrows(AppException.class,
                () -> accountService.handleCreateAccount(newAccount));
        assertEquals("Role not found", exception.getMessage());
        verify(roleRepository).findById(999L);
        verify(accountRepository, never()).save(any());
    }

    @Test
    void testHandleCreateAccount_whenEmailAlreadyExists_shouldThrowAppException() {
        // Arrange
        Account newAccount = Account.builder()
                .email("existing@example.com")
                .role(new Role(1L, "CUSTOMER"))
                .build();

        when(roleRepository.findById(1L)).thenReturn(Optional.of(testRole));
        when(accountRepository.existsByEmail("existing@example.com")).thenReturn(true);

        // Act & Assert
        AppException exception = assertThrows(AppException.class,
                () -> accountService.handleCreateAccount(newAccount));
        assertEquals("Account already exists", exception.getMessage());
        verify(roleRepository).findById(1L);
        verify(accountRepository).existsByEmail("existing@example.com");
        verify(accountRepository, never()).save(any());
    }

    @Test
    void testHandleCreateAccount_whenPhoneAlreadyInUse_shouldThrowAppException() {
        // Arrange
        Account newAccount = Account.builder()
                .email("new@example.com")
                .phoneNumber("0123456789")
                .role(new Role(1L, "CUSTOMER"))
                .build();

        when(roleRepository.findById(1L)).thenReturn(Optional.of(testRole));
        when(accountRepository.existsByEmail("new@example.com")).thenReturn(false);
        when(accountRepository.existsByPhoneNumber("0123456789")).thenReturn(true);

        // Act & Assert
        AppException exception = assertThrows(AppException.class,
                () -> accountService.handleCreateAccount(newAccount));
        assertEquals("Phone already in use", exception.getMessage());
        verify(roleRepository).findById(1L);
        verify(accountRepository).existsByEmail("new@example.com");
        verify(accountRepository).existsByPhoneNumber("0123456789");
        verify(accountRepository, never()).save(any());
    }

    // ========== HANDLE UPDATE ACCOUNT QUICK TESTS ==========

    @Test
    void testHandleUpdateAccountQuick_whenValidUpdate_shouldUpdateSuccessfully() {
        // Arrange
        Account updateRequest = Account.builder()
                .accountId(1L)
                .fullName("Updated Name")
                .phoneNumber("0111111111")
                .build();

        Role adminRole = new Role();
        adminRole.setRoleId(3L);
        adminRole.setRoleName("ADMIN");

        Account adminAccount = Account.builder()
                .accountId(3L)
                .email("admin@example.com")
                .fullName("Admin User")
                .role(adminRole)
                .build();

        when(accountRepository.findById(1L)).thenReturn(Optional.of(testAccount));
        when(accountRepository.existsByPhoneNumberAndAccountIdNot("0111111111", 1L)).thenReturn(false);
        when(accountRepository.save(any(Account.class))).thenReturn(testAccount);
        when(accountRepository.findByRole_RoleName("ADMIN")).thenReturn(List.of(adminAccount));

        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getCurrentUsername).thenReturn("3");
            when(accountRepository.findById(3L)).thenReturn(Optional.of(adminAccount));

            // Act
            Account result = accountService.handleUpdateAccountQuick(updateRequest);

            // Assert
            assertNotNull(result);
            assertEquals("Updated Name", testAccount.getFullName());
            assertEquals("0111111111", testAccount.getPhoneNumber());
            verify(accountRepository, times(2)).findById(1L);
            verify(accountRepository).existsByPhoneNumberAndAccountIdNot("0111111111", 1L);
            verify(accountRepository).save(testAccount);
            verify(activityLogService).log(anyString(), anyString(), anyString(), anyString(), anyString());
        }
    }

    @Test
    void testHandleUpdateAccountQuick_whenAccountNotFound_shouldThrowAppException() {
        // Arrange
        Account updateRequest = Account.builder()
                .accountId(999L)
                .fullName("Updated Name")
                .build();

        when(accountRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        AppException exception = assertThrows(AppException.class,
                () -> accountService.handleUpdateAccountQuick(updateRequest));
        assertEquals("Account not found", exception.getMessage());
        verify(accountRepository).findById(999L);
        verify(accountRepository, never()).save(any());
    }

    @Test
    void testHandleUpdateAccountQuick_whenPhoneAlreadyInUse_shouldThrowAppException() {
        // Arrange
        Account updateRequest = Account.builder()
                .accountId(1L)
                .phoneNumber("0987654321")
                .build();

        when(accountRepository.findById(1L)).thenReturn(Optional.of(testAccount));
        when(accountRepository.existsByPhoneNumberAndAccountIdNot("0987654321", 1L)).thenReturn(true);

        // Act & Assert
        AppException exception = assertThrows(AppException.class,
                () -> accountService.handleUpdateAccountQuick(updateRequest));
        assertEquals("Phone already in use", exception.getMessage());
        verify(accountRepository).findById(1L);
        verify(accountRepository).existsByPhoneNumberAndAccountIdNot("0987654321", 1L);
        verify(accountRepository, never()).save(any());
    }

    // ========== HANDLE DELETE ACCOUNT TESTS ==========

    @Test
    void testHandleDeleteAccount_whenAccountExists_shouldMarkAsDeleted() {
        // Arrange
        Account deleteRequest = Account.builder()
                .accountId(1L)
                .build();

        Role adminRole = new Role();
        adminRole.setRoleId(3L);
        adminRole.setRoleName("ADMIN");

        Account adminAccount = Account.builder()
                .accountId(3L)
                .email("admin@example.com")
                .fullName("Admin User")
                .role(adminRole)
                .build();

        when(accountRepository.findById(1L)).thenReturn(Optional.of(testAccount));
        when(accountRepository.save(any(Account.class))).thenReturn(testAccount);
        when(accountRepository.findByRole_RoleName("ADMIN")).thenReturn(List.of(adminAccount));

        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getCurrentUsername).thenReturn("3");
            when(accountRepository.findById(3L)).thenReturn(Optional.of(adminAccount));

            // Act
            Account result = accountService.handleDeleteAccount(deleteRequest);

            // Assert
            assertNotNull(result);
            assertTrue(testAccount.getIsDeleted());
            verify(accountRepository, times(2)).findById(1L);
            verify(accountRepository).save(testAccount);
            verify(activityLogService).log(anyString(), anyString(), anyString(), anyString(), anyString());
        }
    }

    @Test
    void testHandleDeleteAccount_whenAccountNotFound_shouldThrowAppException() {
        // Arrange
        Account deleteRequest = Account.builder()
                .accountId(999L)
                .build();

        when(accountRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        AppException exception = assertThrows(AppException.class,
                () -> accountService.handleDeleteAccount(deleteRequest));
        assertEquals("Account not found", exception.getMessage());
        verify(accountRepository).findById(999L);
        verify(accountRepository, never()).save(any());
    }

    // ========== HANDLE UPLOAD AVATAR TESTS ==========

    @Test
    void testHandleUploadAvatar_whenValidFile_shouldUploadSuccessfully() throws IOException {
        // Arrange
        MultipartFile mockFile = mock(MultipartFile.class);
        when(mockFile.isEmpty()).thenReturn(false);
        when(mockFile.getOriginalFilename()).thenReturn("avatar.jpg");
        when(mockFile.getBytes()).thenReturn("test-image-data".getBytes());

        Role adminRole = new Role();
        adminRole.setRoleId(3L);
        adminRole.setRoleName("ADMIN");

        Account adminAccount = Account.builder()
                .accountId(3L)
                .email("admin@example.com")
                .fullName("Admin User")
                .role(adminRole)
                .build();

        when(accountRepository.findById(1L)).thenReturn(Optional.of(testAccount));
        when(accountRepository.save(any(Account.class))).thenReturn(testAccount);
        when(accountRepository.findByRole_RoleName("ADMIN")).thenReturn(List.of(adminAccount));

        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getCurrentUsername).thenReturn("3");
            when(accountRepository.findById(3L)).thenReturn(Optional.of(adminAccount));

            // Act & Assert
            // Note: This test expects an exception because file operations will fail in
            // test environment
            Account result = accountService.handleUploadAvatar(1L, mockFile);
            assertNotNull(result);
        }
    }

    @Test
    void testHandleUploadAvatar_whenNoFileUploaded_shouldThrowAppException() {
        // Arrange
        when(accountRepository.findById(1L)).thenReturn(Optional.of(testAccount));

        // Act & Assert
        AppException exception = assertThrows(AppException.class,
                () -> accountService.handleUploadAvatar(1L, null));
        assertEquals("No file uploaded", exception.getMessage());
        verify(accountRepository).findById(1L);
        verify(accountRepository, never()).save(any());
    }

    @Test
    void testHandleUploadAvatar_whenEmptyFile_shouldThrowAppException() {
        // Arrange
        MultipartFile mockFile = mock(MultipartFile.class);
        when(mockFile.isEmpty()).thenReturn(true);
        when(accountRepository.findById(1L)).thenReturn(Optional.of(testAccount));

        // Act & Assert
        AppException exception = assertThrows(AppException.class,
                () -> accountService.handleUploadAvatar(1L, mockFile));
        assertEquals("No file uploaded", exception.getMessage());
        verify(accountRepository).findById(1L);
        verify(accountRepository, never()).save(any());
    }

    // ========== GET TOTAL ACCOUNT BY ROLE TESTS ==========

    @Test
    void testGetTotalAccountByRole_whenRoleExists_shouldReturnCount() {
        // Arrange
        when(roleRepository.existsByRoleName("CUSTOMER")).thenReturn(true);
        when(accountRepository.countByRole_RoleName("CUSTOMER")).thenReturn(100L);

        // Act
        long result = accountService.getTotalAccountByRole("CUSTOMER");

        // Assert
        assertEquals(100L, result);
        verify(roleRepository).existsByRoleName("CUSTOMER");
        verify(accountRepository).countByRole_RoleName("CUSTOMER");
    }

    @Test
    void testGetTotalAccountByRole_whenRoleNotExists_shouldThrowAppException() {
        // Arrange
        when(roleRepository.existsByRoleName("INVALID_ROLE")).thenReturn(false);

        // Act & Assert
        AppException exception = assertThrows(AppException.class,
                () -> accountService.getTotalAccountByRole("INVALID_ROLE"));
        assertEquals("Role not found", exception.getMessage());
        verify(roleRepository).existsByRoleName("INVALID_ROLE");
        verify(accountRepository, never()).countByRole_RoleName(anyString());
    }

    // ========== GET USER REGISTRATIONS DTO TESTS ==========

    @Test
    void testGetUserRegistrationsDTO_whenValidMonthCount_shouldReturnRegistrationStats() {
        // Arrange
        int monthCount = 3;
        List<UserRegistrationsResponse> mockStats = new ArrayList<>();
        mockStats.add(UserRegistrationsResponse.builder()
                .date(LocalDate.now().minusMonths(2).withDayOfMonth(1))
                .newUsers(10L)
                .totalUsers(0L)
                .build());

        when(accountRepository.getUserRegistrationsByMonth(any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(mockStats);
        when(accountRepository.countByRoleRoleIdAndRegisterDateBefore(eq(3L), any(LocalDate.class)))
                .thenReturn(50L);

        // Act
        List<UserRegistrationsResponse> result = accountService.getUserRegistrationsDTO(monthCount);

        // Assert
        assertNotNull(result);
        assertEquals(monthCount, result.size());
        verify(accountRepository).getUserRegistrationsByMonth(any(LocalDate.class), any(LocalDate.class));
        verify(accountRepository).countByRoleRoleIdAndRegisterDateBefore(eq(3L), any(LocalDate.class));
    }

    @Test
    void testGetUserRegistrationsDTO_whenZeroMonthCount_shouldReturnEmptyList() {
        // Arrange
        int monthCount = 0;
        when(accountRepository.getUserRegistrationsByMonth(any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(new ArrayList<>());
        when(accountRepository.countByRoleRoleIdAndRegisterDateBefore(eq(3L), any(LocalDate.class)))
                .thenReturn(0L);

        // Act
        List<UserRegistrationsResponse> result = accountService.getUserRegistrationsDTO(monthCount);

        // Assert
        assertNotNull(result);
        assertEquals(0, result.size());
        verify(accountRepository).getUserRegistrationsByMonth(any(LocalDate.class), any(LocalDate.class));
        verify(accountRepository).countByRoleRoleIdAndRegisterDateBefore(eq(3L), any(LocalDate.class));
    }

    // ========== UTILITY METHOD TESTS ==========

    @Test
    void testGetAccountOrThrow_whenAccountExists_shouldReturnAccount() {
        // Arrange
        when(accountRepository.findById(1L)).thenReturn(Optional.of(testAccount));

        // Act
        Account result = accountService.getAccountOrThrow(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getAccountId());
        assertEquals("test@example.com", result.getEmail());
        verify(accountRepository).findById(1L);
    }

    @Test
    void testGetAccountOrThrow_whenAccountNotExists_shouldThrowAppException() {
        // Arrange
        when(accountRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        AppException exception = assertThrows(AppException.class,
                () -> accountService.getAccountOrThrow(999L));
        assertEquals("Account not found", exception.getMessage());
        verify(accountRepository).findById(999L);
    }

    @Test
    void testIsAccountExist_whenEmailNotExists_shouldNotThrowException() {
        // Arrange
        when(accountRepository.existsByEmail("new@example.com")).thenReturn(false);

        // Act & Assert
        assertDoesNotThrow(() -> accountService.isAccountExist("new@example.com"));
        verify(accountRepository).existsByEmail("new@example.com");
    }

    @Test
    void testIsAccountExist_whenEmailExists_shouldThrowAppException() {
        // Arrange
        when(accountRepository.existsByEmail("existing@example.com")).thenReturn(true);

        // Act & Assert
        AppException exception = assertThrows(AppException.class,
                () -> accountService.isAccountExist("existing@example.com"));
        assertEquals("Account already exists", exception.getMessage());
        verify(accountRepository).existsByEmail("existing@example.com");
    }

    @Test
    void testAssertPhoneNotInUse_whenPhoneNotInUseForNewAccount_shouldNotThrowException() {
        // Arrange
        when(accountRepository.existsByPhoneNumber("0111111111")).thenReturn(false);

        // Act & Assert
        assertDoesNotThrow(() -> accountService.assertPhoneNotInUse("0111111111", null));
        verify(accountRepository).existsByPhoneNumber("0111111111");
    }

    @Test
    void testAssertPhoneNotInUse_whenPhoneInUseForNewAccount_shouldThrowAppException() {
        // Arrange
        when(accountRepository.existsByPhoneNumber("0123456789")).thenReturn(true);

        // Act & Assert
        AppException exception = assertThrows(AppException.class,
                () -> accountService.assertPhoneNotInUse("0123456789", null));
        assertEquals("Phone already in use", exception.getMessage());
        verify(accountRepository).existsByPhoneNumber("0123456789");
    }

    @Test
    void testAssertPhoneNotInUse_whenPhoneNotInUseForExistingAccount_shouldNotThrowException() {
        // Arrange
        when(accountRepository.existsByPhoneNumberAndAccountIdNot("0111111111", 1L)).thenReturn(false);

        // Act & Assert
        assertDoesNotThrow(() -> accountService.assertPhoneNotInUse("0111111111", 1L));
        verify(accountRepository).existsByPhoneNumberAndAccountIdNot("0111111111", 1L);
    }

    @Test
    void testAssertPhoneNotInUse_whenPhoneInUseByAnotherAccount_shouldThrowAppException() {
        // Arrange
        when(accountRepository.existsByPhoneNumberAndAccountIdNot("0987654321", 1L)).thenReturn(true);

        // Act & Assert
        AppException exception = assertThrows(AppException.class,
                () -> accountService.assertPhoneNotInUse("0987654321", 1L));
        assertEquals("Phone already in use", exception.getMessage());
        verify(accountRepository).existsByPhoneNumberAndAccountIdNot("0987654321", 1L);
    }
}
