package com.example.demo.DTO.response.dashboard;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BookingTicketRecentlyResponse {
    private Long bookingId;
    private String fullName;
    private String email;
    private String movieTitle;
    private String cinemaRoomName;
    private LocalDateTime bookingDate;
    private int seatCount;
    private long totalPrice;
    private String paymentMethod;
    private String paymentStatus;

}
