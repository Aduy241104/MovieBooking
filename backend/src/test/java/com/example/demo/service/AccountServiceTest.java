package com.example.demo.service;

import com.example.demo.model.Account;
import com.example.demo.model.Role;
import com.example.demo.repository.AccountRepository;
import com.example.demo.repository.RoleRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {
    @Mock
    private AccountRepository accountRepository;
    @Mock private RoleRepository roleRepository;
    @InjectMocks
    private AccountService accountService;

    @Test
    void handleCreateAccount_shouldThrowException_whenPhoneAlreadyUsed() {
        // Arrange
        Role role = new Role(); role.setRoleId(1L);
        Account account = new Account(); account.setPhoneNumber("0123456789"); account.setRole(role);
        when(roleRepository.findById(1L)).thenReturn(Optional.of(role));
        when(accountRepository.findByPhoneNumber("0123456789")).thenReturn(new Account());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> accountService.handleCreateAccount(account));
        verify(accountRepository).findByPhoneNumber("0123456789");
    }
}
