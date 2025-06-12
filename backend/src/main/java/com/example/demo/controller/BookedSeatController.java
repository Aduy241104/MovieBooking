package com.example.demo.controller;

import com.example.demo.DTO.response.ApiResponse;
import com.example.demo.service.BookedSeatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin
@RequiredArgsConstructor
@RequestMapping("/api/public/booked-seats")
public class BookedSeatController {
    private final BookedSeatService bookedSeatService;

    @GetMapping("/total")
    public ApiResponse<Long> getTotalBookedSeats() {
        return ApiResponse.<Long>builder()
                .status(HttpStatus.OK.value())
                .message("Get Total Booked Seats")
                .result(bookedSeatService.totalBookedSeats())
                .build();
    }

}
