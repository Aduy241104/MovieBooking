package com.example.demo.service;

import com.example.demo.DTO.response.ResPagination;
import com.example.demo.exception.AppException;
import com.example.demo.model.Account;
import com.example.demo.model.Promotion;
import com.example.demo.model.Role;
import com.example.demo.repository.AccountRepository;
import com.example.demo.repository.PromotionRepository;
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

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for PromotionService
 * Testing promotion management operations, validation, and data retrieval
 */
@ExtendWith(MockitoExtension.class)
class PromotionServiceTest {

    @Mock
    private PromotionRepository promotionRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private ActivityLogService activityLogService;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private PromotionService promotionService;

    private Promotion testPromotion;
    private Account testAdminAccount;
    private List<Promotion> testPromotionList;

    @BeforeEach
    void setUp() {
        // Setup test admin account
        Role adminRole = new Role();
        adminRole.setRoleId(1L);
        adminRole.setRoleName("ADMIN");

        testAdminAccount = Account.builder()
                .accountId(1L)
                .email("admin@example.com")
                .fullName("Admin User")
                .role(adminRole)
                .build();

        // Setup test promotion
        testPromotion = Promotion.builder()
                .id(1L)
                .code("TEST2024")
                .discountType("PERCENT")
                .discountLevel(new BigDecimal("20"))
                .maxDiscount(new BigDecimal("100000"))
                .minOrder(new BigDecimal("500000"))
                .startTime(LocalDateTime.now())
                .endTime(LocalDateTime.now().plusDays(30))
                .active(true)
                .isDeleted(false)
                .build();

        // Setup test promotion list
        testPromotionList = new ArrayList<>();
        testPromotionList.add(testPromotion);
        testPromotionList.add(Promotion.builder()
                .id(2L)
                .code("WINTER2024")
                .discountType("VND")
                .discountLevel(new BigDecimal("50000"))
                .maxDiscount(new BigDecimal("50000"))
                .minOrder(new BigDecimal("200000"))
                .startTime(LocalDateTime.now().minusDays(10))
                .endTime(LocalDateTime.now().plusDays(20))
                .active(true)
                .isDeleted(false)
                .build());
    }

    // ========== HANDLE CREATE PROMOTION TESTS ==========

    @Test
    void testHandleCreatePromotion_whenValidPromotion_shouldCreateSuccessfully() {
        // Arrange
        Promotion newPromotion = Promotion.builder()
                .code("NEW2024")
                .discountType("PERCENT")
                .discountLevel(new BigDecimal("15"))
                .maxDiscount(new BigDecimal("75000"))
                .minOrder(new BigDecimal("300000"))
                .startTime(LocalDateTime.now())
                .endTime(LocalDateTime.now().plusDays(30))
                .active(true)
                .build();

        when(promotionRepository.existsByCode("NEW2024")).thenReturn(false);
        when(promotionRepository.save(any(Promotion.class))).thenReturn(newPromotion);
        when(promotionRepository.findById(any())).thenReturn(Optional.of(newPromotion)); // Mock for
                                                                                         // setLogAndNotification
        when(accountRepository.findByRole_RoleName("ADMIN")).thenReturn(List.of(testAdminAccount));

        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getCurrentUsername).thenReturn("1");
            when(accountRepository.findById(1L)).thenReturn(Optional.of(testAdminAccount));

            // Act
            Promotion result = promotionService.handleCreatePromotion(newPromotion);

            // Assert
            assertNotNull(result);
            assertEquals("NEW2024", result.getCode());
            assertEquals("PERCENT", result.getDiscountType());
            assertEquals(new BigDecimal("15"), result.getDiscountLevel());
            assertTrue(result.getActive());

            verify(promotionRepository).existsByCode("NEW2024");
            verify(promotionRepository).save(any(Promotion.class));
            verify(activityLogService).log(anyString(), anyString(), anyString(), anyString(), anyString());
            // Note: notification might not be triggered if admin list check fails
        }
    }

    @Test
    void testHandleCreatePromotion_whenPromotionCodeAlreadyExists_shouldThrowAppException() {
        // Arrange
        Promotion newPromotion = Promotion.builder()
                .code("EXISTING2024")
                .discountType("PERCENT")
                .discountLevel(new BigDecimal("20"))
                .build();

        when(promotionRepository.existsByCode("EXISTING2024")).thenReturn(true);

        // Act & Assert
        AppException exception = assertThrows(AppException.class,
                () -> promotionService.handleCreatePromotion(newPromotion));
        assertEquals("Promotion code already in use", exception.getMessage());

        verify(promotionRepository).existsByCode("EXISTING2024");
        verify(promotionRepository, never()).save(any());
        verify(activityLogService, never()).log(anyString(), anyString(), anyString(), anyString(), anyString());
    }

    // ========== HANDLE UPDATE PROMOTION TESTS ==========

    @Test
    void testHandleUpdatePromotion_whenValidUpdate_shouldUpdateSuccessfully() {
        // Arrange
        Promotion updatePromotion = Promotion.builder()
                .id(1L)
                .code("UPDATED2024")
                .discountType("VND")
                .discountLevel(new BigDecimal("75000"))
                .maxDiscount(new BigDecimal("75000"))
                .minOrder(new BigDecimal("400000"))
                .startTime(LocalDateTime.now())
                .endTime(LocalDateTime.now().plusDays(60))
                .active(true)
                .build();

        when(promotionRepository.findById(1L)).thenReturn(Optional.of(testPromotion));
        when(promotionRepository.existsByCodeAndIdNot("UPDATED2024", 1L)).thenReturn(false);
        when(promotionRepository.save(any(Promotion.class))).thenReturn(testPromotion);
        when(accountRepository.findByRole_RoleName("ADMIN")).thenReturn(List.of(testAdminAccount));

        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getCurrentUsername).thenReturn("1");
            when(accountRepository.findById(1L)).thenReturn(Optional.of(testAdminAccount));

            // Act
            Promotion result = promotionService.handleUpdatePromotion(updatePromotion);

            // Assert
            assertNotNull(result);
            assertEquals("UPDATED2024", testPromotion.getCode());
            assertEquals("VND", testPromotion.getDiscountType());
            assertEquals(new BigDecimal("75000"), testPromotion.getDiscountLevel());

            verify(promotionRepository, times(2)).findById(1L);
            verify(promotionRepository).existsByCodeAndIdNot("UPDATED2024", 1L);
            verify(promotionRepository).save(testPromotion);
            verify(activityLogService).log(anyString(), anyString(), anyString(), anyString(), anyString());
        }
    }

    @Test
    void testHandleUpdatePromotion_whenPromotionNotFound_shouldThrowAppException() {
        // Arrange
        Promotion updatePromotion = Promotion.builder()
                .id(999L)
                .code("UPDATED2024")
                .build();

        when(promotionRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        AppException exception = assertThrows(AppException.class,
                () -> promotionService.handleUpdatePromotion(updatePromotion));
        assertEquals("Promotion not found", exception.getMessage());

        verify(promotionRepository).findById(999L);
        verify(promotionRepository, never()).save(any());
    }

    @Test
    void testHandleUpdatePromotion_whenPromotionCodeAlreadyExists_shouldThrowAppException() {
        // Arrange
        Promotion updatePromotion = Promotion.builder()
                .id(1L)
                .code("EXISTING2024")
                .build();

        when(promotionRepository.findById(1L)).thenReturn(Optional.of(testPromotion));
        when(promotionRepository.existsByCodeAndIdNot("EXISTING2024", 1L)).thenReturn(true);

        // Act & Assert
        AppException exception = assertThrows(AppException.class,
                () -> promotionService.handleUpdatePromotion(updatePromotion));
        assertEquals("Promotion code already in use", exception.getMessage());

        verify(promotionRepository).findById(1L);
        verify(promotionRepository).existsByCodeAndIdNot("EXISTING2024", 1L);
        verify(promotionRepository, never()).save(any());
    }

    // ========== HANDLE UPDATE PROMOTION ACTIVE TESTS ==========

    @Test
    void testHandleUpdatePromotionActive_whenValidPromotionAndStatus_shouldUpdateSuccessfully() {
        // Arrange
        Promotion updatePromotion = Promotion.builder()
                .id(1L)
                .active(false)
                .build();

        when(promotionRepository.findById(1L)).thenReturn(Optional.of(testPromotion));
        when(promotionRepository.save(any(Promotion.class))).thenReturn(testPromotion);

        // Act
        Promotion result = promotionService.handleUpdatePromotionActive(updatePromotion);

        // Assert
        assertNotNull(result);
        assertFalse(testPromotion.getActive());

        verify(promotionRepository).findById(1L);
        verify(promotionRepository).save(testPromotion);
    }

    @Test
    void testHandleUpdatePromotionActive_whenPromotionNotFound_shouldThrowAppException() {
        // Arrange
        Promotion updatePromotion = Promotion.builder()
                .id(999L)
                .active(true)
                .build();

        when(promotionRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        AppException exception = assertThrows(AppException.class,
                () -> promotionService.handleUpdatePromotionActive(updatePromotion));
        assertEquals("Promotion not found", exception.getMessage());

        verify(promotionRepository).findById(999L);
        verify(promotionRepository, never()).save(any());
    }

    // ========== HANDLE DELETE PROMOTION TESTS ==========

    @Test
    void testHandleDeletePromotion_whenPromotionExists_shouldMarkAsDeleted() {
        // Arrange
        Promotion deletePromotion = Promotion.builder()
                .id(1L)
                .build();

        when(promotionRepository.findById(1L)).thenReturn(Optional.of(testPromotion));
        when(promotionRepository.save(any(Promotion.class))).thenReturn(testPromotion);
        when(accountRepository.findByRole_RoleName("ADMIN")).thenReturn(List.of(testAdminAccount));

        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getCurrentUsername).thenReturn("1");
            when(accountRepository.findById(1L)).thenReturn(Optional.of(testAdminAccount));

            // Act
            Promotion result = promotionService.handleDeletePromotion(deletePromotion);

            // Assert
            assertNotNull(result);
            assertTrue(testPromotion.getIsDeleted());

            verify(promotionRepository, times(2)).findById(1L);
            verify(promotionRepository).save(testPromotion);
            verify(activityLogService).log(anyString(), anyString(), anyString(), anyString(), anyString());
        }
    }

    @Test
    void testHandleDeletePromotion_whenPromotionNotFound_shouldThrowAppException() {
        // Arrange
        Promotion deletePromotion = Promotion.builder()
                .id(999L)
                .build();

        when(promotionRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        AppException exception = assertThrows(AppException.class,
                () -> promotionService.handleDeletePromotion(deletePromotion));
        assertEquals("Promotion not found", exception.getMessage());

        verify(promotionRepository).findById(999L);
        verify(promotionRepository, never()).save(any());
    }

    // ========== FETCH ALL PROMOTIONS TESTS ==========

    @Test
    void testFetchAllPromotions_whenPromotionsExist_shouldReturnPaginatedResponse() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Page<Promotion> promotionPage = new PageImpl<>(testPromotionList, pageable, testPromotionList.size());

        when(promotionRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(promotionPage);

        // Act
        ResPagination result = promotionService.fetchAllPromotions(null, pageable);

        // Assert
        assertNotNull(result);
        assertNotNull(result.getMeta());
        assertEquals(1, result.getMeta().getPage());
        assertEquals(10, result.getMeta().getPageSize());
        assertEquals(1, result.getMeta().getPages());
        assertEquals(2L, result.getMeta().getTotal());
        assertEquals(2, ((List<?>) result.getData()).size());

        verify(promotionRepository).findAll(any(Specification.class), eq(pageable));
    }

    @Test
    void testFetchAllPromotions_whenNoPromotionsExist_shouldReturnEmptyPaginatedResponse() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Page<Promotion> emptyPage = new PageImpl<>(new ArrayList<>(), pageable, 0);

        when(promotionRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(emptyPage);

        // Act
        ResPagination result = promotionService.fetchAllPromotions(null, pageable);

        // Assert
        assertNotNull(result);
        assertNotNull(result.getMeta());
        assertEquals(1, result.getMeta().getPage());
        assertEquals(10, result.getMeta().getPageSize());
        assertEquals(0, result.getMeta().getPages());
        assertEquals(0L, result.getMeta().getTotal());
        assertEquals(0, ((List<?>) result.getData()).size());

        verify(promotionRepository).findAll(any(Specification.class), eq(pageable));
    }

    // ========== GET TOTAL ACTIVE PROMOTIONS TESTS ==========

    @Test
    void testGetTotalActivePromotions_whenActivePromotionsExist_shouldReturnCount() {
        // Arrange
        when(promotionRepository.countByActive(true)).thenReturn(5L);

        // Act
        long result = promotionService.getTotalActivePromotions();

        // Assert
        assertEquals(5L, result);
        verify(promotionRepository).countByActive(true);
    }

    @Test
    void testGetTotalActivePromotions_whenNoActivePromotions_shouldReturnZero() {
        // Arrange
        when(promotionRepository.countByActive(true)).thenReturn(0L);

        // Act
        long result = promotionService.getTotalActivePromotions();

        // Assert
        assertEquals(0L, result);
        verify(promotionRepository).countByActive(true);
    }

    // ========== FETCH PROMOTION BY CODE TESTS ==========

    @Test
    void testFetchPromotionByCode_whenPromotionExists_shouldReturnPromotion() {
        // Arrange
        when(promotionRepository.existsByCode("TEST2024")).thenReturn(true);
        when(promotionRepository.findByCode("TEST2024")).thenReturn(testPromotion);

        // Act
        Promotion result = promotionService.fetchPromotionByCode("TEST2024");

        // Assert
        assertNotNull(result);
        assertEquals("TEST2024", result.getCode());
        assertEquals("PERCENT", result.getDiscountType());
        verify(promotionRepository).existsByCode("TEST2024");
        verify(promotionRepository).findByCode("TEST2024");
    }

    @Test
    void testFetchPromotionByCode_whenPromotionNotFound_shouldThrowAppException() {
        // Arrange
        when(promotionRepository.existsByCode("NONEXISTENT")).thenReturn(false);

        // Act & Assert
        AppException exception = assertThrows(AppException.class,
                () -> promotionService.fetchPromotionByCode("NONEXISTENT"));
        assertEquals("Promotion code not found", exception.getMessage());
        verify(promotionRepository).existsByCode("NONEXISTENT");
    }

    @Test
    void testFetchPromotionByCode_whenPromotionInactive_shouldReturnPromotion() {
        // Arrange
        Promotion inactivePromotion = Promotion.builder()
                .id(1L)
                .code("INACTIVE2024")
                .active(false)
                .build();

        when(promotionRepository.existsByCode("INACTIVE2024")).thenReturn(true);
        when(promotionRepository.findByCode("INACTIVE2024")).thenReturn(inactivePromotion);

        // Act
        Promotion result = promotionService.fetchPromotionByCode("INACTIVE2024");

        // Assert
        assertNotNull(result);
        assertEquals("INACTIVE2024", result.getCode());
        assertFalse(result.getActive());
        verify(promotionRepository).existsByCode("INACTIVE2024");
        verify(promotionRepository).findByCode("INACTIVE2024");
    }

    @Test
    void testFetchPromotionByCode_whenPromotionExpired_shouldReturnPromotion() {
        // Arrange
        Promotion expiredPromotion = Promotion.builder()
                .id(1L)
                .code("EXPIRED2024")
                .active(true)
                .startTime(LocalDateTime.now().minusDays(60))
                .endTime(LocalDateTime.now().minusDays(30)) // Expired
                .build();

        when(promotionRepository.existsByCode("EXPIRED2024")).thenReturn(true);
        when(promotionRepository.findByCode("EXPIRED2024")).thenReturn(expiredPromotion);

        // Act
        Promotion result = promotionService.fetchPromotionByCode("EXPIRED2024");

        // Assert
        assertNotNull(result);
        assertEquals("EXPIRED2024", result.getCode());
        assertTrue(result.getActive());
        verify(promotionRepository).existsByCode("EXPIRED2024");
        verify(promotionRepository).findByCode("EXPIRED2024");
    }

    @Test
    void testFetchPromotionByCode_whenPromotionNotStarted_shouldReturnPromotion() {
        // Arrange
        Promotion futurePromotion = Promotion.builder()
                .id(1L)
                .code("FUTURE2024")
                .active(true)
                .startTime(LocalDateTime.now().plusDays(10)) // Not started yet
                .endTime(LocalDateTime.now().plusDays(40))
                .build();

        when(promotionRepository.existsByCode("FUTURE2024")).thenReturn(true);
        when(promotionRepository.findByCode("FUTURE2024")).thenReturn(futurePromotion);

        // Act
        Promotion result = promotionService.fetchPromotionByCode("FUTURE2024");

        // Assert
        assertNotNull(result);
        assertEquals("FUTURE2024", result.getCode());
        assertTrue(result.getActive());
        verify(promotionRepository).existsByCode("FUTURE2024");
        verify(promotionRepository).findByCode("FUTURE2024");
    }

    // Note: calculateDiscount method doesn't exist in the actual service
    // Removing these tests as they were testing non-existent functionality

    // ========== UTILITY METHOD TESTS ==========

    @Test
    void testGetPromotionOrThrow_whenPromotionExists_shouldReturnPromotion() {
        // Arrange
        when(promotionRepository.findById(1L)).thenReturn(Optional.of(testPromotion));

        // Act
        Promotion result = promotionService.getPromotionOrThrow(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("TEST2024", result.getCode());
        verify(promotionRepository).findById(1L);
    }

    @Test
    void testGetPromotionOrThrow_whenPromotionNotExists_shouldThrowAppException() {
        // Arrange
        when(promotionRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        AppException exception = assertThrows(AppException.class,
                () -> promotionService.getPromotionOrThrow(999L));
        assertEquals("Promotion not found", exception.getMessage());
        verify(promotionRepository).findById(999L);
    }

    @Test
    void testAssertPromotionCodeNotInUse_whenCodeNotExists_shouldNotThrowException() {
        // Arrange
        when(promotionRepository.existsByCode("NEWCODE")).thenReturn(false);

        // Act & Assert
        assertDoesNotThrow(() -> promotionService.assertPromotionCodeNotInUse("NEWCODE", null));
        verify(promotionRepository).existsByCode("NEWCODE");
    }

    @Test
    void testAssertPromotionCodeNotInUse_whenCodeExistsForNewPromotion_shouldThrowAppException() {
        // Arrange
        when(promotionRepository.existsByCode("EXISTING")).thenReturn(true);

        // Act & Assert
        AppException exception = assertThrows(AppException.class,
                () -> promotionService.assertPromotionCodeNotInUse("EXISTING", null));
        assertEquals("Promotion code already in use", exception.getMessage());
        verify(promotionRepository).existsByCode("EXISTING");
    }

    @Test
    void testAssertPromotionCodeNotInUse_whenCodeNotUsedByAnotherPromotion_shouldNotThrowException() {
        // Arrange
        when(promotionRepository.existsByCodeAndIdNot("UPDATED", 1L)).thenReturn(false);

        // Act & Assert
        assertDoesNotThrow(() -> promotionService.assertPromotionCodeNotInUse("UPDATED", 1L));
        verify(promotionRepository).existsByCodeAndIdNot("UPDATED", 1L);
    }

    @Test
    void testAssertPromotionCodeNotInUse_whenCodeUsedByAnotherPromotion_shouldThrowAppException() {
        // Arrange
        when(promotionRepository.existsByCodeAndIdNot("DUPLICATE", 1L)).thenReturn(true);

        // Act & Assert
        AppException exception = assertThrows(AppException.class,
                () -> promotionService.assertPromotionCodeNotInUse("DUPLICATE", 1L));
        assertEquals("Promotion code already in use", exception.getMessage());
        verify(promotionRepository).existsByCodeAndIdNot("DUPLICATE", 1L);
    }

    // Note: formatDiscount and setLogAndNotification are private methods
    // Removing these tests as they test private methods that shouldn't be tested
    // directly

}
