package com.example.demo.DTO.response.dashboard;

import lombok.*;

import java.util.Map;

@Getter
@Setter
@Builder
public class DashboardSummaryResponse {

    private DataSummary data;

    @Getter
    @Setter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class DataSummary {
        private Long totalRevenue;
        private Long totalBookings;
        private Long totalUsers;
        private Long activeMovies;
        private Long totalReviews;
        private Double avgRating;
        private Long activePromotions;
        private Long totalCinemaRooms;
        private Map<String, Object> charts;
        private Map<String, Object> tables;
    }

}
