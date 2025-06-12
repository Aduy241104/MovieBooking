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
import com.example.demo.DTO.response.SingleMovieDTO;
import com.example.demo.model.Screening;
import com.example.demo.repository.ScreeningRepository;
import com.example.demo.service.MovieScheduleService;
import com.example.demo.service.ScreeningService;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/public/movieSchedule")
public class MovieScheduleController {

    @Autowired
    ScreeningRepository repository;

    @Autowired
    ScreeningService screeningService;

    @Autowired
    MovieScheduleService movieScheduleService;

    @GetMapping("/getAll")
    public ApiResponse<List<Screening>> getMethodName() {
        List<Screening> response = repository.findAll();
        return ApiResponse.<List<Screening>>builder()
                .result(response)
                .build();
    }

    @GetMapping("/by-date")
    public ApiResponse<List<MovieScheduleDTO>> getScheduleByDate(
            @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        List<MovieScheduleDTO> result = movieScheduleService.getSchedule(date);
        return ApiResponse.<List<MovieScheduleDTO>>builder()
                .message("success")
                .result(result)
                .build();
    }

    @GetMapping("/now-showing")
    public ApiResponse<List<SingleMovieDTO>> getNowShowing() {
        List<SingleMovieDTO> response = movieScheduleService.getNowShowingMovie(LocalDate.now());
        return ApiResponse.<List<SingleMovieDTO>>builder()
                .message("success")
                .result(response)
                .build();

    }

    @GetMapping("/up-coming")
    public ApiResponse<List<SingleMovieDTO>> getUpComing() {
        List<SingleMovieDTO> response = movieScheduleService.getCommingSoonMovie(LocalDate.now());
        return ApiResponse.<List<SingleMovieDTO>>builder()
                .message("success")
                .result(response)
                .build();

    }

    @GetMapping("/{id}")
    public ApiResponse<SingleMovieDTO> getMethodName(@PathVariable Long id) {
        SingleMovieDTO singleMovieDTO = movieScheduleService.getMovieDetail(id);
        return ApiResponse.<SingleMovieDTO>builder()
                .message("success")
                .result(singleMovieDTO)
                .build();
    }

}
