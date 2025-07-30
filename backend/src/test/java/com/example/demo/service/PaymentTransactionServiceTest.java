package com.example.demo.service;

import com.example.demo.DTO.response.PaymentTransactionResponseDTO;
import com.example.demo.model.*;
import com.example.demo.repository.BookingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.data.domain.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PaymentTransactionServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    @InjectMocks
    private PaymentTransactionService paymentTransactionService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetAllTransactions_ReturnsPaginatedDTOs() {
        // Arrange
        int page = 0;
        int size = 2;
        Pageable pageable = PageRequest.of(page, size);

        // Mock Booking data
        Booking booking1 = new Booking();
        booking1.setId(1);
        Account account1 = new Account();
        account1.setFullName("John Doe");
        booking1.setAccount(account1);
        PaymentMethod method1 = new PaymentMethod();
        method1.setName("Credit Card");
        booking1.setPaymentMethod(method1);
        booking1.setTotalAmount(new BigDecimal("100.00"));
        booking1.setBookingStatus("SUCCESS");
        booking1.setBookingTime(LocalDateTime.now());

        Booking booking2 = new Booking();
        booking2.setId(2);
        booking2.setAccount(null); // test null case
        booking2.setPaymentMethod(null);
        booking2.setTotalAmount(new BigDecimal("50.00"));
        booking2.setBookingStatus("FAILED");
        booking2.setBookingTime(LocalDateTime.now());

        List<Booking> bookings = List.of(booking1, booking2);
        Page<Booking> mockPage = new PageImpl<>(bookings, pageable, bookings.size());

        when(bookingRepository.findAll(pageable)).thenReturn(mockPage);

        // Act
        Page<PaymentTransactionResponseDTO> result = paymentTransactionService.getAllTransactions(page, size);

        // Assert
        assertEquals(2, result.getContent().size());
        assertEquals("John Doe", result.getContent().get(0).getAccountName());
        assertEquals("N/A", result.getContent().get(1).getAccountName()); // null account handled
        verify(bookingRepository, times(1)).findAll(pageable);
    }
}
