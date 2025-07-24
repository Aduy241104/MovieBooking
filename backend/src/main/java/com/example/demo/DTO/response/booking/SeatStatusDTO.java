package com.example.demo.DTO.response.booking;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SeatStatusDTO {
    private Long seatId;
    private String seatRow;
    private String seatCol;
    private String seatTypeName;
    private BigDecimal seatTypePrice;
    private String status;
    private Long seatTypeId;
    private LocalDateTime expiresAt;
}