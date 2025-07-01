package com.example.demo.DTO.response.booking;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SeatStatusDTO {
    private Long seatId;
    private String seatRow;
    private String seatCol;
    private String seatTypeName;
    private BigDecimal seatTypePrice; // Phụ thu của loại ghế
    private String status; // "Available", "Booked", "Selecting" (tùy chọn)
    private Long seatTypeId;
}