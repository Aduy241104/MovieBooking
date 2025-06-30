package com.example.demo.controller;

import com.example.demo.DTO.response.ApiResponse;
import com.example.demo.DTO.response.dashboard.DailyTicketRevenueResponse;
import com.example.demo.DTO.response.BookingResponse;
import com.example.demo.service.BookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin
@RequiredArgsConstructor
@RequestMapping("/api/public/bookings")
public class BookingController {
    private final BookingService bookingService;

    @GetMapping("/revenue")
    public ApiResponse<Long> getTotalRevenueByStatus() {
        Long totalRevenue = bookingService.getTotalRevenueByStatus("PAID");
        return ApiResponse.<Long>builder()
                .status(1000)
                .message("Lấy tổng doanh thu thành công")
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

    @GetMapping("/getAll")
    public ApiResponse<List<BookingResponse>> getAllBookings() {
        List<BookingResponse> bookings = bookingService.getAllBookings();
        return ApiResponse.<List<BookingResponse>>builder()
                .status(1000)
                .message("Lấy tất cả booking thành công")
                .result(bookings)
                .build();
    }

    @GetMapping("/by-movie/{movieId}")
    public ApiResponse<List<BookingResponse>> getBookingsByMovieId(@PathVariable Long movieId) {
        List<BookingResponse> bookings = bookingService.getBookingsByMovieId(movieId);
        return ApiResponse.<List<BookingResponse>>builder()
                .status(1000)
                .message("Lấy danh sách booking theo phim thành công")
                .result(bookings)
                .build();
    }
}