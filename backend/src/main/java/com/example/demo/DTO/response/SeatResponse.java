package com.example.demo.DTO.response;

import lombok.Data;

@Data
public class SeatResponse {
    private String seatCol;
    private int seatRow;
    private String seatType; // "REGULAR", "VIP", "DOUBLE"
}
