package com.example.demo.controller;

import com.example.demo.DTO.response.dashboard.DashboardSummaryResponse;
import com.example.demo.DTO.response.dashboard.MovieTypeRevenueResponse;
import com.example.demo.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin
@RequiredArgsConstructor
@RequestMapping("/api/public/dashboard")
public class DashboardController {
    private final DashboardService dashboardService;

    @GetMapping("/summary")
    public DashboardSummaryResponse getDashboardSummary() {
        return dashboardService.getDashboardSummary();
    }

    @GetMapping("/movie-type-revenue")
    public MovieTypeRevenueResponse getMovieTypeRevenue() {
        return dashboardService.getMovieTypeRevenue();
    }
}
