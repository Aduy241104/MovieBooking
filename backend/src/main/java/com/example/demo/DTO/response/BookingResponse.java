package com.example.demo.DTO.response;

import com.example.demo.model.Account;
import com.example.demo.model.PaymentMethod;
import com.example.demo.model.Screening;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingResponse {
    private Integer id; // Changed from bookingId to match the "id" field in the example
    private Account account; // Nested Account object
    private Screening screening; // Nested Screening object
    private PaymentMethod paymentMethod; // Nested PaymentMethod object
    private String promotionCodeApplied;
    private String discountTypeApplied;
    private BigDecimal discountApplied;
    private LocalDateTime bookingTime; // Changed from bookingDate to match the example
    private BigDecimal totalAmount;
    private String bookingStatus;
    private Integer seatCount;
}