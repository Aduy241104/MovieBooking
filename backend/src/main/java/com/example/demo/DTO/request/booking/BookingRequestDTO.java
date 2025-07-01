package com.example.demo.DTO.request.booking;

import lombok.Data;

import java.util.List;

@Data
public class BookingRequestDTO {
    private Long screeningId;
    private List<Long> seatIds;
    private String promotionCode; // Optional
    private Long paymentMethodId; // ID của phương thức thanh toán
    private int pointsToUse;
}