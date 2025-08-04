package com.example.demo.DTO.response;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PaymentTransactionResponseDTO {
    private Integer bookingId;
    private String accountName;
    private String paymentMethod;
    private BigDecimal totalAmount;
    private String bookingStatus;
    private LocalDateTime bookingTime;
}