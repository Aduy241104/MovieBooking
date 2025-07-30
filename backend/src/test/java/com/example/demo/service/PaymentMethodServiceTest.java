package com.example.demo.service;

import com.example.demo.model.PaymentMethod;
import com.example.demo.repository.PaymentMethodRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Sort;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class PaymentMethodServiceTest {

    @Mock
    private PaymentMethodRepository paymentMethodRepository;

    @InjectMocks
    private PaymentMethodService paymentMethodService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testFindAll() {
        PaymentMethod method1 = new PaymentMethod(1L, "Cash", "", true);
        PaymentMethod method2 = new PaymentMethod(2L, "Card", "", true);
        when(paymentMethodRepository.findAll(Sort.by(Sort.Direction.ASC, "id"))).thenReturn(Arrays.asList(method1, method2));

        List<PaymentMethod> result = paymentMethodService.findAll();
        assertEquals(2, result.size());
        verify(paymentMethodRepository).findAll(Sort.by(Sort.Direction.ASC, "id"));
    }

    @Test
    void testSave_WithNullActive_SetsActiveTrue() {
        PaymentMethod method = new PaymentMethod(null, "Momo", "", null);
        when(paymentMethodRepository.existsByNameIgnoreCase("Momo")).thenReturn(false);
        when(paymentMethodRepository.save(any(PaymentMethod.class))).thenReturn(method);

        PaymentMethod saved = paymentMethodService.save(method);
        assertTrue(saved.getActive());
    }

    @Test
    void testSave_DuplicateName_ThrowsException() {
        PaymentMethod method = new PaymentMethod(null, "Cash", "", null);
        when(paymentMethodRepository.existsByNameIgnoreCase("Cash")).thenReturn(true);

        assertThrows(RuntimeException.class, () -> paymentMethodService.save(method));
    }

    @Test
    void testUpdate_Valid() {
        PaymentMethod existing = new PaymentMethod(1L, "Old", "Old desc", true);
        PaymentMethod updated = new PaymentMethod(1L, "New", "New desc", true);

        when(paymentMethodRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(paymentMethodRepository.save(existing)).thenReturn(existing);

        PaymentMethod result = paymentMethodService.update(1L, updated);
        assertEquals("New", result.getName());
        assertEquals("New desc", result.getDescription());
    }

    @Test
    void testUpdate_NotFound_ThrowsException() {
        when(paymentMethodRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> paymentMethodService.update(1L, new PaymentMethod()));
    }

    @Test
    void testToggleActive() {
        PaymentMethod method = new PaymentMethod(1L, "Cash", "desc", true);
        when(paymentMethodRepository.findById(1L)).thenReturn(Optional.of(method));
        when(paymentMethodRepository.save(method)).thenReturn(method);

        PaymentMethod result = paymentMethodService.toggleActive(1L);
        assertFalse(result.getActive());
    }

    @Test
    void testToggleActive_NotFound_ThrowsException() {
        when(paymentMethodRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> paymentMethodService.toggleActive(1L));
    }

    @Test
    void testDelete() {
        paymentMethodService.delete(1L);
        verify(paymentMethodRepository).deleteById(1L);
    }

    @Test
    void testGetActivePaymentMethods() {
        PaymentMethod method = new PaymentMethod(1L, "Cash", "desc", true);
        when(paymentMethodRepository.findByActiveTrue()).thenReturn(List.of(method));

        List<PaymentMethod> result = paymentMethodService.getActivePaymentMethods();
        assertEquals(1, result.size());
    }

}