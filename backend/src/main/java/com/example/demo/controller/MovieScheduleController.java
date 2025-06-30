package com.example.demo.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.example.demo.DTO.request.ScreeningRequest;
import com.example.demo.DTO.response.ApiResponse;
import com.example.demo.DTO.response.MovieScheduleDTO;
import com.example.demo.DTO.response.SingleMovieDTO;
import com.example.demo.model.Screening;
import com.example.demo.repository.ScreeningRepository;
import com.example.demo.service.MovieScheduleService;
import com.example.demo.service.ScreeningService;

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
                .message("Thành công")
                .result(result)
                .build();
    }

    @GetMapping("/now-showing")
    public ApiResponse<List<SingleMovieDTO>> getNowShowing() {
        List<SingleMovieDTO> response = movieScheduleService.getNowShowingMovie(LocalDate.now());
        return ApiResponse.<List<SingleMovieDTO>>builder()
                .message("Thành công")
                .result(response)
                .build();
    }

    @GetMapping("/up-coming")
    public ApiResponse<List<SingleMovieDTO>> getUpComing() {
        List<SingleMovieDTO> response = movieScheduleService.getCommingSoonMovie(LocalDate.now());
        return ApiResponse.<List<SingleMovieDTO>>builder()
                .message("Thành công")
                .result(response)
                .build();
    }

    @GetMapping("/{id}")
    public ApiResponse<SingleMovieDTO> getMethodName(@PathVariable Long id) {
        SingleMovieDTO singleMovieDTO = movieScheduleService.getMovieDetail(id);
        return ApiResponse.<SingleMovieDTO>builder()
                .message("Thành công")
                .result(singleMovieDTO)
                .build();
    }

    @GetMapping("/now-showing/total")
    public ApiResponse<Long> totalNowShowingMovie() {
        Long response = movieScheduleService.getTotalNowShowingMovie();
        return ApiResponse.<Long>builder()
                .status(HttpStatus.OK.value())
                .message("Lấy tổng số phim đang chiếu")
                .result(response)
                .build();
    }

    @PostMapping("/admin/add-time")
    public ApiResponse<Screening> addScreening(@RequestBody ScreeningRequest request) {
        try {
            Screening screening = screeningService.addScreening(request);
            return ApiResponse.<Screening>builder()
                    .status(HttpStatus.CREATED.value())
                    .message("Thêm lịch chiếu thành công")
                    .result(screening)
                    .build();
        } catch (IllegalArgumentException e) {
            return ApiResponse.<Screening>builder()
                    .status(HttpStatus.BAD_REQUEST.value())
                    .message(e.getMessage())
                    .build();
        }
    }

    @PutMapping("/admin/update-time/{id}")
    public ApiResponse<Screening> updateScreening(@PathVariable Long id, @RequestBody ScreeningRequest request) {
        try {
            Screening screening = screeningService.updateScreening(id, request);
            return ApiResponse.<Screening>builder()
                    .status(HttpStatus.OK.value())
                    .message("Cập nhật lịch chiếu thành công")
                    .result(screening)
                    .build();
        } catch (IllegalArgumentException e) {
            return ApiResponse.<Screening>builder()
                    .status(HttpStatus.BAD_REQUEST.value())
                    .message(e.getMessage())
                    .build();
        }
    }

    @DeleteMapping("/admin/delete-time/{id}")
    public ApiResponse<Void> softDeleteScreening(@PathVariable Long id) {
        try {
            screeningService.softDeleteScreening(id);
            return ApiResponse.<Void>builder()
                    .status(HttpStatus.OK.value())
                    .message("Xóa lịch chiếu thành công")
                    .build();
        } catch (IllegalArgumentException e) {
            return ApiResponse.<Void>builder()
                    .status(HttpStatus.BAD_REQUEST.value())
                    .message(e.getMessage())
                    .build();
        }
    }

    @GetMapping("/admin/all-active")
    public ApiResponse<List<Screening>> getAllActiveScreenings() {
        List<Screening> screenings = screeningService.getAllActiveScreenings();
        return ApiResponse.<List<Screening>>builder()
                .status(HttpStatus.OK.value())
                .message("Danh sách lịch chiếu chưa bị xóa mềm")
                .result(screenings)
                .build();
    }
}