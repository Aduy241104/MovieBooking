package com.example.demo.DTO.response.dashboard;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingTicketRecentlyResponse {
    private Integer bookingId;
    private String fullName;
    private String email;
    private String movieTitle;
    private String cinemaRoomName;
    private LocalDateTime bookingDate;
    private Long seatCount;
    private BigDecimal totalPrice;
    private String paymentMethod;
    private String paymentStatus;
}
