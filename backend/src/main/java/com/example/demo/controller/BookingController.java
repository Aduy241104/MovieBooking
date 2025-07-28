package com.example.demo.controller;

import com.example.demo.DTO.request.booking.BookingRequestDTO;
import com.example.demo.DTO.response.ApiResponse;
import com.example.demo.DTO.response.booking.BookingDetailResponseDTO;
import com.example.demo.DTO.response.dashboard.DailyTicketRevenueResponse;
import com.example.demo.model.Account;
import com.example.demo.model.Promotion;
import com.example.demo.service.AccountService;
import com.example.demo.service.BookingService;
import com.example.demo.service.PromotionService;
import com.example.demo.utils.SecurityUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@CrossOrigin
@RequiredArgsConstructor
@RequestMapping("/api/bookings")
public class BookingController {
    private final BookingService bookingService;
    private final PromotionService promotionService;
    private final AccountService accountService;

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
    //booking

    @PostMapping
    public ApiResponse<BookingDetailResponseDTO> createBooking(@RequestBody BookingRequestDTO bookingRequest, HttpServletRequest httpServletRequest) {
        String accountIdStr = SecurityContextHolder.getContext().getAuthentication().getName();
        Long accountId = Long.parseLong(accountIdStr);

        BookingDetailResponseDTO bookingDetail = bookingService.createBooking(bookingRequest, accountId, httpServletRequest);
        return ApiResponse.<BookingDetailResponseDTO>builder()
                .status(HttpStatus.CREATED.value())
                .message("Booking created successfully. Status: " + bookingDetail.getBookingStatus())
                .result(bookingDetail)
                .build();
    }


    @GetMapping("/promotions/check/{code}")
    public ApiResponse<Promotion> checkPromotion(@PathVariable String code) {
        Promotion promotion = promotionService.findAndValidatePromotion(code);
        return ApiResponse.<Promotion>builder()
                .status(HttpStatus.OK.value())
                .message("Áp dụng mã khuyến mãi thành công!")
                .result(promotion)
                .build();
    }

    // API callback VNPAY
    @GetMapping("/payment/vnpay_return")
    public RedirectView vnpayReturn(HttpServletRequest request) {
        Map<String, String> params = request.getParameterMap().entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue()[0]));

        String redirectUrl = bookingService.handleVnpayReturn(params);
        System.out.println("BookingController - Redirecting to frontend URL: " + redirectUrl);
        return new RedirectView(redirectUrl);
    }

    @GetMapping("/history")
    public ApiResponse<List<BookingDetailResponseDTO>> getUserBookingHistory() {
        String accountIdStr = SecurityContextHolder.getContext().getAuthentication().getName();
        Long accountId = Long.parseLong(accountIdStr);
        List<BookingDetailResponseDTO> history = bookingService.getUserBookingHistory(accountId);
        return ApiResponse.<List<BookingDetailResponseDTO>>builder()
                .status(HttpStatus.OK.value())
                .message("Successfully fetched booking history.")
                .result(history)
                .build();
    }

    @GetMapping("/{bookingId}/details")
    public ApiResponse<BookingDetailResponseDTO> getBookingDetails(@PathVariable Integer bookingId) {
        String accountIdStr = SecurityContextHolder.getContext().getAuthentication().getName();
        Long accountId = Long.parseLong(accountIdStr);
        BookingDetailResponseDTO bookingDetail = bookingService.getBookingDetailsForUser(bookingId, accountId);
        return ApiResponse.<BookingDetailResponseDTO>builder()
                .status(HttpStatus.OK.value())
                .message("Successfully fetched booking details.")
                .result(bookingDetail)
                .build();
    }

    @GetMapping("/points")
    public ApiResponse<Integer> getCurrentUserPoints() {
        String accountIdStr = SecurityUtils.getCurrentUsername();
        Long accountId = Long.parseLong(accountIdStr);
        Account account = accountService.fetchAccountById(accountId);
        return ApiResponse.<Integer>builder()
                .status(HttpStatus.OK.value())
                .message("User points fetched successfully.")
                .result(account.getScore())
                .build();
    }

    @PostMapping("/{bookingId}/retry-payment")
    public ApiResponse<String> retryPayment(
            @PathVariable Integer bookingId,
            HttpServletRequest httpServletRequest) {

        String accountIdStr = SecurityUtils.getCurrentUsername();
        Long accountId = Long.parseLong(accountIdStr);

        String paymentUrl = bookingService.retryPayment(bookingId, accountId, httpServletRequest);

        return ApiResponse.<String>builder()
                .status(HttpStatus.OK.value())
                .message("New payment URL created successfully.")
                .result(paymentUrl)
                .build();
    }

    @GetMapping("/movie/{movieId}")
    public ApiResponse<List<BookingDetailResponseDTO>> getAllBookingsByMovieId(@PathVariable Long movieId) {
        List<BookingDetailResponseDTO> bookings = bookingService.getAllBookingsByMovieId(movieId);
        return ApiResponse.<List<BookingDetailResponseDTO>>builder()
                .status(HttpStatus.OK.value())
                .message("Successfully fetched all bookings for movie ID: " + movieId)
                .result(bookings)
                .build();
    }

    @GetMapping("/movie/{movieId}/count")
    public ApiResponse<Long> getTotalBookingsByMovieId(@PathVariable Long movieId) {
        Long totalBookings = bookingService.getTotalBookingsByMovieId(movieId);
        return ApiResponse.<Long>builder()
                .status(HttpStatus.OK.value())
                .message("Total bookings for movie ID: " + movieId)
                .result(totalBookings)
                .build();
    }
}