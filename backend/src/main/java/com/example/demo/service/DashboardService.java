package com.example.demo.service;

import com.example.demo.DTO.response.dashboard.*;
import com.example.demo.exception.AppException;
import com.example.demo.repository.MovieRepository;
import com.example.demo.repository.MovieTypeRepository;
import com.example.demo.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardService {
    private final MovieScheduleService movieScheduleService;
    private final BookingService bookingService;
    private final ReviewService reviewService;
    private final PromotionService promotionService;
    private final BookedSeatService bookedSeatService;
    private final AccountService accountService;
    private final CinemaRoomService cinemaRoomService;
    private final MovieTypeRepository movieTypeRepository;
    private final MovieRepository movieRepository;
    private final RoleRepository roleRepository;

    /**
     * Get revenue statistics by movie type.
     *
     * @return MovieTypeRevenueResponse containing the revenue statistics by movie
     * type.
     */
    public MovieTypeRevenueResponse getMovieTypeRevenue() {
        List<MovieTypeRevenueResponse.MovieTypeRevenue> stats = movieTypeRepository.getMoviesByTypeRevenue();
        return MovieTypeRevenueResponse.builder()
                .data(stats)
                .build();
    }

    /**
     * Get all dashboard summary data.
     *
     * @return DashboardSummaryResponse containing the summary data.
     */
    public DashboardSummaryResponse getDashboardSummary() {
        String roleName = "CUSTOMER";
        // ========== Get overview statistics ==========
        Long totalRevenue = bookingService.getTotalRevenueByStatus("PAID");
        Long totalBookings = bookedSeatService.totalBookedSeats();
        boolean isRoleExist = roleRepository.existsByRoleName(roleName);
        if (!isRoleExist) {
            throw new AppException("Role not found: " + roleName);
        }
        Long totalCustomers = accountService.getTotalAccountByRole(roleName);
        Long nowShowingMovies = movieScheduleService.getTotalNowShowingMovie();
        Long totalReviews = reviewService.getTotalApprovedReviews();
        Double avgRating = reviewService.findAverageRatingOfApproved();
        Long totalPromotions = promotionService.getTotalActivePromotions();
        Long totalCinemaRooms = cinemaRoomService.getTotalCinemaRoom();
        // ===========================================

        // Get revenue statistics for day, week, month
        List<DailyTicketRevenueResponse> dailyRevenueList = bookingService.getDailyTicketRevenue(7);
        List<DailyTicketRevenueResponse> weeklyRevenueList = bookingService.getWeeklyTicketRevenue(6);
        List<DailyTicketRevenueResponse> monthlyRevenueList = bookingService.getMonthlyTicketRevenue(6);
        // Get revenue statistics by movie type
        MovieTypeRevenueResponse movieTypeRevenueResponse = getMovieTypeRevenue();
        // Get user registrations statistics
        List<UserRegistrationsResponse> userRegistrationsResponse = accountService.getUserRegistrationsDTO(3);
        // Get revenue statistics for top movies
        List<TopMovieByRevenueResponse> topMoviesData = movieRepository.getTopMoviesByRevenue();

        // Build DTO for Charts
        DashboardSummaryResponse.ChartsData charts = DashboardSummaryResponse.ChartsData.builder()
                .revenue(DashboardSummaryResponse.RevenueChartGroup.builder()
                        .day(dailyRevenueList)
                        .week(weeklyRevenueList)
                        .month(monthlyRevenueList)
                        .build())
                .moviesByType(movieTypeRevenueResponse.getData()) // Use List DTO directly
                .userRegistrations(userRegistrationsResponse.stream()
                        .map(i -> DashboardSummaryResponse.UserRegistrationChartData.builder()
                                .date(i.getDate().format(
                                        DateTimeFormatter.ofPattern("MM/yyyy")))
                                .newUsers(i.getNewUsers())
                                .totalUsers(i.getTotalUsers())
                                .build())
                        .collect(Collectors.toList()))
                .build();

        // Build DTO for Tables
        DashboardSummaryResponse.TablesData tables = DashboardSummaryResponse.TablesData.builder()
                .topMovies(topMoviesData) // Use List DTO directly
                .recentBookings(bookingService.getRecentlyBookedTickets(30))
                .build();

        // Build the final response
        DashboardSummaryResponse.DataSummary dataSummary = DashboardSummaryResponse.DataSummary.builder()
                .totalRevenue(totalRevenue)
                .totalBookings(totalBookings)
                .totalUsers(totalCustomers)
                .activeMovies(nowShowingMovies)
                .totalReviews(totalReviews)
                .avgRating(avgRating)
                .activePromotions(totalPromotions)
                .totalCinemaRooms(totalCinemaRooms)
                .charts(charts)
                .tables(tables)
                .build();
        return DashboardSummaryResponse.builder()
                .data(dataSummary)
                .build();
    }
}
