package com.example.demo.DTO.response.dashboard;

import lombok.*;

import java.util.List;

@Data
@Builder
public class DashboardSummaryResponse {

    private DataSummary data;

    @Data
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
        private ChartsData charts;
        private TablesData tables;
    }

    @Getter
    @Setter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ChartsData {
        private RevenueChartGroup revenue;
        private List<MovieTypeRevenueResponse.MovieTypeRevenue> moviesByType;
        private List<UserRegistrationChartData> userRegistrations;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class RevenueChartGroup {
        private List<DailyTicketRevenueResponse> day;
        private List<DailyTicketRevenueResponse> week;
        private List<DailyTicketRevenueResponse> month;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class UserRegistrationChartData {
        private String date;
        private Long newUsers;
        private Long totalUsers;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class TablesData {
        private List<TopMovieByRevenueResponse> topMovies;
        private List<BookingTicketRecentlyResponse> recentBookings;
    }

}
