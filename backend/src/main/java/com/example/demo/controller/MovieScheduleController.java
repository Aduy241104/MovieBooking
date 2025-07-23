package com.example.demo.controller;

import java.time.LocalDate;
import java.util.List;

import com.example.demo.DTO.response.SingleMovieDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import com.example.demo.DTO.request.ScreeningRequest;
import com.example.demo.DTO.response.ApiResponse;
import com.example.demo.DTO.response.MovieScheduleDTO;
import com.example.demo.model.Account;
import com.example.demo.model.Screening;
import com.example.demo.path.MovieSchedulePath;
import com.example.demo.repository.ScreeningRepository;
import com.example.demo.service.AccountService;
import com.example.demo.service.MovieScheduleService;
import com.example.demo.service.ScreeningService;

import lombok.extern.slf4j.Slf4j;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/public/movieSchedule")
@Slf4j
public class MovieScheduleController {

    @Autowired
    ScreeningRepository repository;

    @Autowired
    ScreeningService screeningService;

    @Autowired
    MovieScheduleService movieScheduleService;

    @Autowired
    AccountService accountService;

    @GetMapping(MovieSchedulePath.GET_ALL)
    public ApiResponse<List<Screening>> getMethodName() {
        List<Screening> response = repository.findAll();
        return ApiResponse.<List<Screening>>builder()
                .result(response)
                .build();
    }

    @GetMapping(MovieSchedulePath.BY_DATE)
    public ApiResponse<List<MovieScheduleDTO>> getScheduleByDate(
            @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        List<MovieScheduleDTO> result = movieScheduleService.getSchedule(date);
        return ApiResponse.<List<MovieScheduleDTO>>builder()
                .message("success")
                .result(result)
                .build();
    }

    @GetMapping(MovieSchedulePath.NOW_SHOWING)
    public ApiResponse<List<SingleMovieDTO>> getNowShowing() {
        List<SingleMovieDTO> response = movieScheduleService.getNowShowingMovie(LocalDate.now());
        return ApiResponse.<List<SingleMovieDTO>>builder()
                .message("success")
                .result(response)
                .build();
    }

    @GetMapping(MovieSchedulePath.UPCOMING)
    public ApiResponse<List<SingleMovieDTO>> getUpComing() {
        List<SingleMovieDTO> response = movieScheduleService.getCommingSoonMovie(LocalDate.now());
        return ApiResponse.<List<SingleMovieDTO>>builder()
                .message("success")
                .result(response)
                .build();
    }

    @GetMapping(MovieSchedulePath.DETAIL)
    public ApiResponse<SingleMovieDTO> getMethodName(@PathVariable Long id) {
        SingleMovieDTO singleMovieDTO = movieScheduleService.getMovieDetail(id);
        return ApiResponse.<SingleMovieDTO>builder()
                .message("success")
                .result(singleMovieDTO)
                .build();
    }

    @GetMapping(MovieSchedulePath.TOTAL_NOW_SHOWING)
    public ApiResponse<Long> totalNowShowingMovie() {
        Long response = movieScheduleService.getTotalNowShowingMovie();
        return ApiResponse.<Long>builder()
                .status(HttpStatus.OK.value())
                .message("Get total now showing movie")
                .result(response)
                .build();
    }

    @GetMapping("/acc")
    public ApiResponse<List<Account>> getMethod() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();

        log.info("User info: {}", authentication.getName());
        authentication.getAuthorities().forEach(grantedAuthority -> log.info(grantedAuthority.getAuthority()));

        List<Account> accounts = accountService.getAllAccount();
        return ApiResponse.<List<Account>>builder()
                .result(accounts)
                .build();
    }

    @GetMapping(MovieSchedulePath.TOP_BOOKING)
    public ApiResponse<List<SingleMovieDTO>> getTopBookedMovie() {
        List<SingleMovieDTO> listMovie = movieScheduleService.getTopBookedMovieByDate();
        return ApiResponse.<List<SingleMovieDTO>>builder()
                .message("success")
                .result(listMovie)
                .build();
    }

    // ADMIN API

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
