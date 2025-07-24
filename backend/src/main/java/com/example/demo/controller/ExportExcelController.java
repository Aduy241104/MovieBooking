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
import org.springframework.format.annotation.DateTimeFormat; // Thêm import này


@RestController
@RequestMapping("/api/admin")
@CrossOrigin
@RequiredArgsConstructor
public class ExportExcelController {

    private final BookingRepository bookingRepository;
    private final ExportExcelService exportExcelService;

    // <<< THAY THẾ API CŨ BẰNG API MỚI NÀY >>>
    @GetMapping("/export/excel")
    public ResponseEntity<InputStreamResource> exportBookingsToExcel(
            @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) throws IOException {

        // Xác định khoảng thời gian từ đầu ngày bắt đầu đến cuối ngày kết thúc
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);

        // Lấy danh sách booking thành công trong khoảng thời gian đã chọn
        List<Booking> bookings = bookingRepository.findByBookingStatusInAndBookingTimeBetweenOrderByBookingTimeAsc(
                Arrays.asList("PAID", "RESERVED"),
                startDateTime,
                endDateTime
        );

        // Tạo file Excel trong bộ nhớ
        String sheetName = "Bookings_" + startDate.toString() + "_to_" + endDate.toString();
        ByteArrayInputStream in = exportExcelService.bookingsToExcel(bookings, sheetName);

        // Thiết lập headers
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