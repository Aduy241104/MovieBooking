package com.example.demo.controller;

import com.example.demo.DTO.response.ApiResponse;
import com.example.demo.DTO.response.dashboard.DailyTicketRevenueResponse;
import com.example.demo.service.BookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@CrossOrigin
@RequiredArgsConstructor
@RequestMapping("/api/bookings")
public class BookingController {
    private final BookingService bookingService;

    @GetMapping("/revenue")
    public ApiResponse<Long> getTotalRevenueByStatus() {
        Long totalRevenue = bookingService.getTotalRevenueByStatus("PAID");
        return ApiResponse.<Long>builder()
                .status(HttpStatus.OK.value())
                .message("Total revenue fetched successfully")
                .result(totalRevenue)
                .build();
    }

    @GetMapping("/daily-revenue")
    public List<DailyTicketRevenueResponse> getDailyTicketRevenue() {
        return bookingService.getDailyTicketRevenue(7);
    }

    @GetMapping("/weekly-revenue")
    public List<DailyTicketRevenueResponse> getWeeklyTicketRevenue() {
        return bookingService.getWeeklyTicketRevenue(6);
    }

    @GetMapping("/monthly-revenue")
    public List<DailyTicketRevenueResponse> getMonthlyTicketRevenue() {
        return bookingService.getMonthlyTicketRevenue(6);
    }
}
