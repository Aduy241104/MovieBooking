package com.example.demo.service;

import com.example.demo.exception.DuplicateNameException;
import com.example.demo.exception.NotFoundException;
import com.example.demo.exception.UnauthorizedException;
import com.example.demo.model.Account;
import com.example.demo.model.Type;
import com.example.demo.repository.AccountRepository;
import com.example.demo.repository.TypeRepository;
import com.example.demo.utils.SecurityUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TypeServiceTest {

    @InjectMocks
    private TypeService typeService;

    @Mock
    private TypeRepository typeRepository;
    @Mock
    private AccountRepository accountRepository;
    @Mock
    private ActivityLogService activityLogService;
    @Mock
    private NotificationService notificationService;

    private final Type type = new Type(1L, "Hành động", false);
    private final Account user = new Account();
    private final Account admin = new Account();

    @BeforeEach
    void setUp() {
        user.setAccountId(1L);
        user.setEmail("user@example.com");
        user.setFullName("Người Dùng");

        admin.setAccountId(2L);
        admin.setEmail("admin@example.com");
        admin.setFullName("Quản trị");
    }

    // region ADD TYPE

    @Test
    void addType_shouldSucceed() {
        try (MockedStatic<SecurityUtils> mocked = mockStatic(SecurityUtils.class)) {
            mocked.when(SecurityUtils::getCurrentUsername).thenReturn("1");

            when(typeRepository.existsByName("Hành động")).thenReturn(false);
            when(accountRepository.findById(1L)).thenReturn(Optional.of(user));
            when(accountRepository.findByRole_RoleName("ADMIN")).thenReturn(List.of(admin));
            when(typeRepository.save(any())).thenReturn(type);

            Type result = typeService.addType(type);

            assertEquals("Hành động", result.getName());
            verify(activityLogService).log(any(), any(), any(), any(), any());
            verify(notificationService).notify(any(), any(), any(), any());
        }
    }

    @Test
    void addType_shouldThrowDuplicateNameException() {
        when(typeRepository.existsByName("Hành động")).thenReturn(true);

        assertThrows(DuplicateNameException.class, () -> typeService.addType(type));
        verify(typeRepository, never()).save(any());
    }

    @Test
    void addType_shouldThrowUnauthorizedIfNoUser() {
        try (MockedStatic<SecurityUtils> mocked = mockStatic(SecurityUtils.class)) {
            mocked.when(SecurityUtils::getCurrentUsername).thenReturn(null);
            when(typeRepository.existsByName("Hành động")).thenReturn(false);

            assertThrows(UnauthorizedException.class, () -> typeService.addType(type));
        }
    }

    // endregion

    // region UPDATE TYPE

    @Test
    void updateType_shouldSucceed() {
        try (MockedStatic<SecurityUtils> mocked = mockStatic(SecurityUtils.class)) {
            mocked.when(SecurityUtils::getCurrentUsername).thenReturn("1");

            Type updated = new Type(1L, "Hài", false);

            when(typeRepository.findById(1)).thenReturn(Optional.of(type));
            when(typeRepository.existsByNameAndIdNot("Hài", 1)).thenReturn(false);
            when(accountRepository.findById(1L)).thenReturn(Optional.of(user));
            when(accountRepository.findByRole_RoleName("ADMIN")).thenReturn(List.of(admin));
            when(typeRepository.save(any())).thenReturn(updated);

            Type result = typeService.updateType(1, updated);

            assertEquals("Hài", result.getName());
        }
    }

    @Test
    void updateType_shouldThrowNotFoundException() {
        when(typeRepository.findById(1)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> typeService.updateType(1, type));
    }

    @Test
    void updateType_shouldThrowDuplicateNameException() {
        when(typeRepository.findById(1)).thenReturn(Optional.of(type));
        when(typeRepository.existsByNameAndIdNot("Hành động", 1)).thenReturn(true);

        assertThrows(DuplicateNameException.class, () -> typeService.updateType(1, type));
    }

    @Test
    void updateType_shouldThrowUnauthorizedIfAccountMissing() {
        try (MockedStatic<SecurityUtils> mocked = mockStatic(SecurityUtils.class)) {
            mocked.when(SecurityUtils::getCurrentUsername).thenReturn("1");

            when(typeRepository.findById(1)).thenReturn(Optional.of(type));
            when(typeRepository.existsByNameAndIdNot("Hành động", 1)).thenReturn(false);
            when(accountRepository.findById(1L)).thenReturn(Optional.empty());

            assertThrows(UnauthorizedException.class, () -> typeService.updateType(1, type));
        }
    }

    // endregion

    // region DELETE TYPE

    @Test
    void deleteType_shouldSucceed() {
        try (MockedStatic<SecurityUtils> mocked = mockStatic(SecurityUtils.class)) {
            mocked.when(SecurityUtils::getCurrentUsername).thenReturn("1");

            when(typeRepository.findById(1)).thenReturn(Optional.of(type));
            when(accountRepository.findById(1L)).thenReturn(Optional.of(user));
            when(accountRepository.findByRole_RoleName("ADMIN")).thenReturn(List.of(admin));

            typeService.deleteType(1);

            assertTrue(type.getIsDeleted());
            verify(typeRepository).save(type);
        }
    }

    @Test
    void deleteType_shouldThrowNotFoundException() {
        when(typeRepository.findById(1)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> typeService.deleteType(1));
    }

    // endregion

    // region GET ALL + GET BY ID

    @Test
    void getAllTypes_shouldReturnList() {
        when(typeRepository.findByIsDeletedFalse()).thenReturn(List.of(type));
        List<Type> types = typeService.getAllTypes();

        assertEquals(1, types.size());
        assertEquals("Hành động", types.get(0).getName());
    }

    @Test
    void getTypeById_shouldReturnOptional() {
        when(typeRepository.findById(1)).thenReturn(Optional.of(type));
        Optional<Type> result = typeService.getTypeById(1);

        assertTrue(result.isPresent());
        assertEquals("Hành động", result.get().getName());
    }

    // endregion
}
