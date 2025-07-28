package com.example.demo.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import com.example.demo.DTO.response.PaymentTransactionResponseDTO;
import com.example.demo.model.Booking;
import com.example.demo.repository.BookingRepository;

import org.springframework.data.domain.Pageable;

/**
 * Service class responsible for handling payment transaction-related logic.
 */
@Service
public class PaymentTransactionService {

    @Autowired
    private BookingRepository bookingRepository;


     /**
     * Retrieves a paginated list of all payment transactions in the system.
     *
     * @param page the page number to retrieve (zero-based)
     * @param size the number of items per page
     * @return a paginated list of {@link PaymentTransactionResponseDTO}
     */
    public Page<PaymentTransactionResponseDTO> getAllTransactions(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Booking> bookingPage = bookingRepository.findAll(pageable);

        List<PaymentTransactionResponseDTO> dtos = bookingPage.getContent().stream()
                .map(booking -> new PaymentTransactionResponseDTO(
                        booking.getId(),
                        booking.getAccount() != null ? booking.getAccount().getFullName() : "N/A",
                        booking.getPaymentMethod() != null ? booking.getPaymentMethod().getName() : "N/A",
                        booking.getTotalAmount(),
                        booking.getBookingStatus(),
                        booking.getBookingTime()))
                .collect(Collectors.toList());

        return new PageImpl<>(dtos, pageable, bookingPage.getTotalElements());
    }

}
