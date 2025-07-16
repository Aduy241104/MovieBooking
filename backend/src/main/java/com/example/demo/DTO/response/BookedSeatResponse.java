package com.example.demo.DTO.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BookedSeatResponse {
    private Long id;
    private Long bookingId;
    private Long seatId;
    private BigDecimal pricePaid;
}