package com.example.demo.DTO.response.dashboard;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DailyTicketRevenueResponse {
    private LocalDate date;
    private Long revenue;
    private Long tickets;

}
