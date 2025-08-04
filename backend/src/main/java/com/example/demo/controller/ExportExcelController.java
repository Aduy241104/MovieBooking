package com.example.demo.controller;
import com.example.demo.model.Booking;
import com.example.demo.repository.BookingRepository;
import com.example.demo.service.ExportExcelService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;


@RestController
@RequestMapping("/api/admin")
@CrossOrigin
@RequiredArgsConstructor
public class ExportExcelController {

    private final BookingRepository bookingRepository;
    private final ExportExcelService exportExcelService;


    @GetMapping("/export/excel")
    public ResponseEntity<InputStreamResource> exportBookingsToExcel(
            @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) throws IOException {

        // Specifies the time period from the start of the start date to the end of the end date
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);

        // Get a list of successful bookings within the selected time period
        List<Booking> bookings = bookingRepository.findByBookingStatusInAndBookingTimeBetweenOrderByBookingTimeAsc(
                Arrays.asList("PAID", "RESERVED"),
                startDateTime,
                endDateTime
        );

        // Create Excel file in memory
        String sheetName = "Bookings_" + startDate.toString() + "_to_" + endDate.toString();
        ByteArrayInputStream in = exportExcelService.bookingsToExcel(bookings, sheetName);

        // Headers
        HttpHeaders headers = new HttpHeaders();
        String fileName = "bookings_" + startDate.toString() + "_to_" + endDate.toString() + ".xlsx";
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName);

        return ResponseEntity
                .ok()
                .headers(headers)
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheet.sheet"))
                .body(new InputStreamResource(in));
    }
}