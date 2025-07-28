package com.example.demo.DTO.response.dashboard;

import lombok.*;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DailyTicketRevenueResponse {
    private Timestamp date;
    private BigDecimal revenue;
    private Long tickets;

    public LocalDateTime getDate() {
        return date.toLocalDateTime();
    }
}
