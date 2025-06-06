package com.example.demo.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.DTO.response.ApiResponse;
import com.example.demo.DTO.response.MovieScheduleDTO;
import com.example.demo.model.Screening;
import com.example.demo.repository.ScreeningRepository;
import com.example.demo.service.ScreeningService;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/public/movieSchedule")
public class MovieScheduleController {

    @Autowired
    ScreeningRepository repository;

    @Autowired
    ScreeningService screeningService;

    @GetMapping("/getAll")
    public ApiResponse<List<Screening>> getMethodName() {
        List<Screening> response = repository.findAll();
        return ApiResponse.<List<Screening>>builder()
                .result(response)
                .build();
    }

    @GetMapping("/by-date")
    public ResponseEntity<List<MovieScheduleDTO>> getScheduleByDate(
            @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        List<MovieScheduleDTO> result = screeningService.getAllMovieScheduleByDate(date);
        return ResponseEntity.ok(result);
    }

}
