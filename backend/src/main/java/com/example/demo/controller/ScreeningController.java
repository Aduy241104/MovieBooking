//LocPNG

package com.example.demo.controller;

import com.example.demo.DTO.response.ApiResponse;
import com.example.demo.DTO.response.booking.ScreeningScheduleResponseDTO;
import com.example.demo.DTO.response.booking.SeatStatusDTO;
import com.example.demo.service.ScreeningService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/public/screenings")
@CrossOrigin
@RequiredArgsConstructor
public class ScreeningController {

    private final ScreeningService screeningService;

    @GetMapping("/movie/{movieId}")
    public ApiResponse<ScreeningScheduleResponseDTO> getMovieSchedules(@PathVariable Long movieId) {
        return ApiResponse.<ScreeningScheduleResponseDTO>builder()
                .status(HttpStatus.OK.value())
                .message("Successfully fetched schedules for movie " + movieId)
                .result(screeningService.getSchedulesForMovie(movieId))
                .build();
    }

    @GetMapping("/{screeningId}/seat-status")
    public ApiResponse<List<SeatStatusDTO>> getSeatStatus(@PathVariable Long screeningId) {
        return ApiResponse.<List<SeatStatusDTO>>builder()
                .status(HttpStatus.OK.value())
                .message("Successfully fetched seat status for screening " + screeningId)
                .result(screeningService.getSeatStatusForScreening(screeningId))
                .build();
    }
}