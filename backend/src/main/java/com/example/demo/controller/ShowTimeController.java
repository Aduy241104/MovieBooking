package com.example.demo.controller;

import com.example.demo.DTO.request.ScreeningRequest;
import com.example.demo.DTO.response.ApiResponse;
import com.example.demo.model.Screening;
import com.example.demo.service.ScreeningService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/admin/movieSchedule")
public class ShowTimeController {

    @Autowired
    private ScreeningService screeningService; // Inject service to handle screening-related logic

    // API to add a new screening
    @PostMapping("/add-time")
    public ApiResponse<Screening> addScreening(@RequestBody ScreeningRequest request) {
        try {
            Screening screening = screeningService.addScreening(request); // Call service to add screening
            return ApiResponse.<Screening>builder()
                    .status(HttpStatus.CREATED.value()) // Return 201 Created
                    .message("Screening added successfully")
                    .result(screening)
                    .build();
        } catch (IllegalArgumentException e) { // Catch invalid request error
            return ApiResponse.<Screening>builder()
                    .status(HttpStatus.BAD_REQUEST.value()) // 400 Bad Request
                    .message(e.getMessage())
                    .build();
        }
    }

    // API to update a screening by ID
    @PutMapping("/update-time/{id}")
    public ApiResponse<Screening> updateScreening(@PathVariable Long id, @RequestBody ScreeningRequest request) {
        try {
            Screening screening = screeningService.updateScreening(id, request); // Call service to update screening
            return ApiResponse.<Screening>builder()
                    .status(HttpStatus.OK.value()) // 200 OK
                    .message("Screening updated successfully")
                    .result(screening)
                    .build();
        } catch (IllegalArgumentException e) { // Catch if not found or invalid data
            return ApiResponse.<Screening>builder()
                    .status(HttpStatus.BAD_REQUEST.value())
                    .message(e.getMessage())
                    .build();
        }
    }

    // API to soft delete a screening (only updates isDelete status)
    @DeleteMapping("/delete-time/{id}")
    public ApiResponse<Void> softDeleteScreening(@PathVariable Long id) {
        try {
            screeningService.softDeleteScreening(id); // Call service to soft delete screening
            return ApiResponse.<Void>builder()
                    .status(HttpStatus.OK.value())
                    .message("Screening deleted successfully")
                    .build();
        } catch (IllegalArgumentException e) {
            return ApiResponse.<Void>builder()
                    .status(HttpStatus.BAD_REQUEST.value())
                    .message(e.getMessage())
                    .build();
        }
    }

    // API to get all screenings that are not soft-deleted (isDelete = false)
    @GetMapping("/list-isdelete")
    public ApiResponse<List<Screening>> getAllActiveScreenings() {
        List<Screening> screenings = screeningService.getAllActiveScreenings(); // Call service to fetch the list
        return ApiResponse.<List<Screening>>builder()
                .status(HttpStatus.OK.value())
                .message("List of active (non-deleted) screenings")
                .result(screenings)
                .build();
    }
}
