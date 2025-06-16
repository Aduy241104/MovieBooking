package com.example.demo.service;

import com.example.demo.DTO.response.dashboard.*;
import com.example.demo.model.Role;
import com.example.demo.repository.MovieRepository;
import com.example.demo.repository.MovieTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DashboardService {
    private final MovieScheduleService movieScheduleService;
    private final BookingService bookingService;
    private final ReviewService reviewService;
    private final PromotionService promotionService;
    private final BookedSeatService bookedSeatService;
    private final AccountService accountService;
    private final RoleService roleService;
    private final CinemaRoomService cinemaRoomService;
    private final MovieTypeRepository movieTypeRepository;
    private final MovieRepository movieRepository;


    /**
     * Lấy doanh thu theo loại phim
     *
     * @return MovieTypeRevenueDTO chứa danh sách doanh thu theo loại phim
     */
    public MovieTypeRevenueResponse getMovieTypeRevenue() {
        List<Object[]> stats = movieTypeRepository.getMoviesByTypeRevenue();
        List<MovieTypeRevenueResponse.MovieTypeRevenue> result = new ArrayList<>();
        for (Object[] row : stats) {
            String movieType = (String) row[0];
            int quantity = ((Number) row[1]).intValue();
            Long revenue = ((Number) row[2]).longValue();
            result.add(new MovieTypeRevenueResponse.MovieTypeRevenue(movieType, quantity, revenue));
        }
        return MovieTypeRevenueResponse.builder()
                .data(result)
                .build();
    }

    public List<TopMovieByRevenueResponse> getTopMovieByRevenue() {
        List<Object[]> stats = movieRepository.getTopMoviesByRevenue();
        List<TopMovieByRevenueResponse> result = new ArrayList<>();
        for (Object[] row : stats) {
            String movieTitle = (String) row[0];
            String movieGenre = (String) row[1];
            double averageRating = ((Number) row[2]).doubleValue();
            int ticketSold = ((Number) row[3]).intValue();
            long revenue = ((Number) row[4]).longValue();
            String imageUrl = (String) row[5];
            result.add(new TopMovieByRevenueResponse(
                    movieTitle,
                    movieGenre,
                    averageRating,
                    ticketSold,
                    revenue,
                    imageUrl
            ));
        }
        return result;
    }

    /**
     * Lấy dữ liệu tổng quan cho dashboard
     *
     * @return DashboardSummaryDTO chứa dữ liệu tổng quan
     */
    public DashboardSummaryResponse getDashboardSummary() {
        // Lấy dữ liệu tổng quan
        Long totalRevenue = bookingService.getTotalRevenueByStatus("PAID");
        Long totalBookings = bookedSeatService.totalBookedSeats();
        Role role = roleService.findRoleByName("CUSTOMER");
        if (role == null) {
            throw new RuntimeException("Role not found");
        }
        Long totalCustomers = accountService.getTotalAccountByRole(role);
        Long nowShowingMovies = movieScheduleService.getTotalNowShowingMovie();
        Long totalReviews = reviewService.getTotalApprovedReviews();
        Double avgRating = reviewService.findAverageRatingOfApproved();
        Long totalPromotions = promotionService.getTotalActivePromotions();
        Long totalCinemaRooms = cinemaRoomService.getTotalCinemaRoom();

        // Lấy dữ liệu doanh thu hàng ngày
        List<DailyTicketRevenueResponse> dailyRevenueList = bookingService.getDailyTicketRevenue(7);
        // Lấy dữ liệu doanh thu hàng tuần
        List<DailyTicketRevenueResponse> weeklyRevenueList = bookingService.getWeeklyTicketRevenue(6);
        // Lấy dữ liệu doanh thu hàng tháng
        List<DailyTicketRevenueResponse> monthlyRevenueList = bookingService.getMonthlyTicketRevenue(6);
        // Lấy dữ liệu doanh thu theo loại phim
        MovieTypeRevenueResponse movieTypeRevenueResponse = getMovieTypeRevenue();
        // Lấy dữ liệu người dùng đăng ký theo tháng
        UserRegistrationsResponse userRegistrationsResponse = accountService.getUserRegistrationsDTO(6);
        // Định dạng ngày tháng
//        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM");

        // Tạo danh sách revenue cho từng loại (day, week, month)
        List<Map<String, Object>> dayRevenue = dailyRevenueList.stream()
                .map(i -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("name", i.getDate());
                    map.put("revenue", i.getRevenue());
                    map.put("bookings", i.getTickets());
                    return map;
                }).toList();
        List<Map<String, Object>> weekRevenue = weeklyRevenueList.stream()
                .map(i -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("name", i.getDate());
                    map.put("revenue", i.getRevenue());
                    map.put("bookings", i.getTickets());
                    return map;
                }).toList();
        List<Map<String, Object>> monthRevenue = monthlyRevenueList.stream()
                .map(i -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("name", i.getDate());
                    map.put("revenue", i.getRevenue());
                    map.put("bookings", i.getTickets());
                    return map;
                }).toList();

        // Tạo danh sách doanh thu theo loại phim
        List<Map<String, Object> > movieTypeRevenue = movieTypeRevenueResponse.getData().stream()
                .map(i -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("name", i.getMovieType());
                    map.put("count", i.getQuantity());
                    map.put("revenue", i.getRevenue());
                    return map;
                }).toList();

        // Tạo danh sách người dùng đăng ký theo tháng
        List<Map<String, Object>> userRegistrations = userRegistrationsResponse.getData().stream()
                .map(i -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("name", i.getDate().format(DateTimeFormatter.ofPattern("MM/yyyy")));
                    map.put("newUsers", i.getNewUsers());
                    map.put("totalUsers", i.getTotalUsers());
                    return map;
                }).toList();

        // Tạo map revenue
        Map<String, Object> revenue = new HashMap<>();
        revenue.put("day", dayRevenue);
        revenue.put("week", weekRevenue);
        revenue.put("month", monthRevenue);

        // Tạo map charts
        Map<String, Object> charts = new HashMap<>();
        charts.put("revenue", revenue);
        charts.put("moviesByType", movieTypeRevenue);
        charts.put("userRegistrations", userRegistrations);

        // ==========

        // Lấy danh sách phim đang chiếu
        List<Map<String, Object>> topMovies = getTopMovieByRevenue().stream()
                .map(i -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("title", i.getMovieTitle());
                    map.put("genre", i.getMovieGenre());
                    map.put("rating", i.getAvgRating());
                    map.put("totalBookings", i.getTicketSold());
                    map.put("revenue", i.getRevenue());
                    map.put("poster", i.getPosterUrl());
                    return map;
                }).toList();

        // Tạo map tables
        Map<String, Object> tables = new HashMap<>();
        tables.put("topMovies", topMovies);
        tables.put("recentBookings", bookingService.getRecentlyBookedTickets(30));

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
