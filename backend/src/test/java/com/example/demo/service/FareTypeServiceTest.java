package com.example.demo.service;

import com.example.demo.DTO.request.FareTypeRequest;
import com.example.demo.DTO.response.ResPagination;
import com.example.demo.model.Account;
import com.example.demo.model.FareType;
import com.example.demo.repository.AccountRepository;
import com.example.demo.repository.FareTypeRepository;
import com.example.demo.utils.SecurityUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FareTypeServiceTest {

    @Mock
    private FareTypeRepository fareTypeRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private ActivityLogService activityLogService;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private FareTypeService fareTypeService;

    private FareType fareType;
    private FareTypeRequest fareTypeRequest;
    private Account mockAccount;

    @BeforeEach
    void setUp() {
        fareType = new FareType();
        fareType.setId(1L);
        fareType.setName("Adult");
        fareType.setBasePrice(new BigDecimal("100000"));
        fareType.setDayPrice(new BigDecimal("120000"));
        fareType.setTimeSlotType("Evening");
        fareType.setMovieFormat("2D");
        fareType.setIsDeleted(false);

        fareTypeRequest = new FareTypeRequest();
        fareTypeRequest.setName("Adult");
        fareTypeRequest.setBasePrice(new BigDecimal("100000"));
        fareTypeRequest.setDayPrice(new BigDecimal("120000"));
        fareTypeRequest.setTimeSlotType("Evening");
        fareTypeRequest.setMovieFormat("2D");

        mockAccount = new Account();
        mockAccount.setAccountId(99L);
        mockAccount.setEmail("test@example.com");
        mockAccount.setFullName("Test User");
    }

    @Test
    void handleCreateFareType_success() {
        try (MockedStatic<SecurityUtils> mockedSecurityUtils = Mockito.mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getCurrentUsername).thenReturn("99");

            when(accountRepository.findById(99L)).thenReturn(Optional.of(mockAccount));
            when(accountRepository.findByRole_RoleName("ADMIN")).thenReturn(Collections.singletonList(mockAccount));

            // ✅ Giả lập DB tự gán ID khi save
            when(fareTypeRepository.save(any(FareType.class))).thenAnswer(invocation -> {
                FareType saved = invocation.getArgument(0);
                saved.setId(1L);
                return saved;
            });

            // ✅ Cần thêm mock nếu `setLogAndNotification` gọi lại `findById`
            when(fareTypeRepository.findById(1L)).thenReturn(Optional.of(fareType));

            FareType result = fareTypeService.handleCreateFareType(fareTypeRequest);

            assertNotNull(result);
            assertEquals("Adult", result.getName());

            ArgumentCaptor<FareType> captor = ArgumentCaptor.forClass(FareType.class);
            verify(fareTypeRepository).save(captor.capture());
            FareType saved = captor.getValue();

            assertEquals("Adult", saved.getName());
            assertEquals(new BigDecimal("100000"), saved.getBasePrice());
            assertEquals(new BigDecimal("120000"), saved.getDayPrice());
            assertEquals("Evening", saved.getTimeSlotType());
            assertEquals("2D", saved.getMovieFormat());
            assertFalse(saved.getIsDeleted());
        }
    }


    @Test
    void handleUpdateFareType_success() {
        try (MockedStatic<SecurityUtils> mockedSecurityUtils = Mockito.mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getCurrentUsername).thenReturn("99");

            when(fareTypeRepository.findById(1L)).thenReturn(Optional.of(fareType));
            when(accountRepository.findById(99L)).thenReturn(Optional.of(mockAccount));
            when(accountRepository.findByRole_RoleName("ADMIN")).thenReturn(Collections.singletonList(mockAccount));
            when(fareTypeRepository.save(any(FareType.class))).thenReturn(fareType);

            FareType result = fareTypeService.handleUpdateFareType(fareTypeRequest, 1L);

            assertNotNull(result);
            assertEquals("Adult", result.getName());

            // 👇 Capture and verify updated data
            ArgumentCaptor<FareType> captor = ArgumentCaptor.forClass(FareType.class);
            verify(fareTypeRepository).save(captor.capture());
            FareType updated = captor.getValue();

            assertEquals("Adult", updated.getName());
            assertEquals("Evening", updated.getTimeSlotType());
            assertEquals("2D", updated.getMovieFormat());
            assertEquals(new BigDecimal("120000"), updated.getDayPrice());
        }
    }

    @Test
    void handleDeleteFareType_success() {
        try (MockedStatic<SecurityUtils> mockedSecurityUtils = Mockito.mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getCurrentUsername).thenReturn("99");

            when(fareTypeRepository.findById(1L)).thenReturn(Optional.of(fareType));
            when(accountRepository.findById(99L)).thenReturn(Optional.of(mockAccount));
            when(accountRepository.findByRole_RoleName("ADMIN")).thenReturn(Collections.singletonList(mockAccount));
            when(fareTypeRepository.save(any(FareType.class))).thenReturn(fareType);

            FareType result = fareTypeService.handleDeleteFareType(1L);

            assertNotNull(result);
            assertTrue(result.getIsDeleted());

            // 👇 Verify that isDeleted was set to true
            ArgumentCaptor<FareType> captor = ArgumentCaptor.forClass(FareType.class);
            verify(fareTypeRepository).save(captor.capture());
            FareType deleted = captor.getValue();

            assertTrue(deleted.getIsDeleted());
        }
    }

    @Test
    void fetchFareTypeById_success() {
        when(fareTypeRepository.findById(1L)).thenReturn(Optional.of(fareType));
        FareType result = fareTypeService.fetchFareTypeById(1L);
        assertNotNull(result);
        assertEquals("Adult", result.getName());
    }

    @Test
    void fetchFareTypeById_notFound() {
        when(fareTypeRepository.findById(1L)).thenReturn(Optional.empty());
        Exception e = assertThrows(RuntimeException.class, () -> fareTypeService.fetchFareTypeById(1L));
        assertEquals("Không tìm thấy loại giá với ID: 1", e.getMessage());
    }

    @Test
    void fetchAllFareTypes_success() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<FareType> page = new PageImpl<>(List.of(fareType), pageable, 1);

        // ✅ Fix: specify Specification.class to avoid ambiguity
        when(fareTypeRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);

        ResPagination result = fareTypeService.fetchAllFareTypes(null, pageable);

        assertNotNull(result);
        assertEquals(1, result.getMeta().getPage());
        assertEquals(1, ((List<?>) result.getData()).size());
    }

    @Test
    void fetchFareTypeByName_success() {
        when(fareTypeRepository.findByNameAndIsDeletedFalse("Adult")).thenReturn(fareType);
        FareType result = fareTypeService.fetchFareTypeByName("Adult");
        assertNotNull(result);
    }

    @Test
    void existsByName_success() {
        when(fareTypeRepository.existsByName("Adult")).thenReturn(true);
        boolean result = fareTypeService.existsByName("Adult");
        assertTrue(result);
    }

    @Test
    void fetchFareTypeByIsDeletedFalse_success() {
        when(fareTypeRepository.findByIsDeletedFalse()).thenReturn(List.of(fareType));
        List<FareType> result = fareTypeService.fetchFareTypeByIsDeletedFalse();
        assertEquals(1, result.size());
    }
}