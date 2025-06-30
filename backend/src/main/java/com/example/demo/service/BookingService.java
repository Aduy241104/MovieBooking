package com.example.demo.service;

import com.example.demo.DTO.response.dashboard.BookingTicketRecentlyResponse;
import com.example.demo.DTO.response.dashboard.DailyTicketRevenueResponse;
import com.example.demo.repository.BookingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class BookingService {
    private final BookingRepository bookingRepository;

    public Long getTotalRevenueByStatus(String status) {
        return bookingRepository.getTotalRevenueByStatus(status);
    }

    public List<DailyTicketRevenueResponse> getDailyTicketRevenue(int dailyCount) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime fromDate = now.minusDays(dailyCount).toLocalDate().atStartOfDay();
        LocalDateTime toDate = now.toLocalDate().atStartOfDay();

        List<Object[]> stats = bookingRepository.getDailyTicketRevenue(fromDate, toDate);

        // Map ngày -> dữ liệu
        Map<LocalDate, DailyTicketRevenueResponse> map = new HashMap<>();
        for (Object[] row : stats) {
            LocalDate date = ((java.sql.Timestamp) row[0]).toLocalDateTime().toLocalDate();
            Long revenue = ((Number) row[1]).longValue();
            Long tickets = ((Number) row[2]).longValue();
            map.put(date, new DailyTicketRevenueResponse(date, revenue, tickets));
        }
        // Fill đủ 7 ngày
        List<DailyTicketRevenueResponse> result = new ArrayList<>();
        for (int i = 0; i < dailyCount; i++) {
            LocalDate date = fromDate.plusDays(i).toLocalDate();
            DailyTicketRevenueResponse resp = map.getOrDefault(
                    date,
                    new DailyTicketRevenueResponse(date, 0L, 0L));
            result.add(resp);
        }
        return result;
    }

    public List<DailyTicketRevenueResponse> getWeeklyTicketRevenue(int weekCount) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime fromDate = now.minusWeeks(weekCount).with(java.time.DayOfWeek.MONDAY).toLocalDate()
                .atStartOfDay();
        LocalDateTime toDate = now.with(DayOfWeek.MONDAY).toLocalDate().atStartOfDay();

        List<Object[]> stats = bookingRepository.getWeeklyRevenueAndTickets(fromDate, toDate);

        Map<LocalDate, DailyTicketRevenueResponse> map = new HashMap<>();
        for (Object[] row : stats) {
            LocalDate weekStart = ((java.sql.Timestamp) row[0]).toLocalDateTime().toLocalDate();
            Long revenue = ((Number) row[1]).longValue();
            Long tickets = ((Number) row[2]).longValue();
            map.put(weekStart, new DailyTicketRevenueResponse(weekStart, revenue, tickets));
        }
        List<DailyTicketRevenueResponse> result = new ArrayList<>();
        for (int i = 0; i < weekCount; i++) {
            LocalDate weekStart = fromDate.plusWeeks(i).with(DayOfWeek.MONDAY).toLocalDate();
            DailyTicketRevenueResponse resp = map.getOrDefault(
                    weekStart,
                    new DailyTicketRevenueResponse(weekStart, 0L, 0L));
            result.add(resp);
        }
        return result;
    }

    public List<DailyTicketRevenueResponse> getMonthlyTicketRevenue(int monthCount) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime fromDate = now.minusMonths(monthCount).withDayOfMonth(1).toLocalDate().atStartOfDay();
        LocalDateTime toDate = now.withDayOfMonth(1).toLocalDate().atStartOfDay();

        List<Object[]> stats = bookingRepository.getMonthlyRevenueAndTickets(fromDate, toDate);

        Map<LocalDate, DailyTicketRevenueResponse> map = new HashMap<>();
        for (Object[] row : stats) {
            LocalDate monthStart = ((java.sql.Timestamp) row[0]).toLocalDateTime().toLocalDate();
            Long revenue = ((Number) row[1]).longValue();
            Long tickets = ((Number) row[2]).longValue();
            map.put(monthStart, new DailyTicketRevenueResponse(monthStart, revenue, tickets));
        }
        List<DailyTicketRevenueResponse> result = new ArrayList<>();
        for (int i = 0; i < monthCount; i++) {
            LocalDate monthStart = fromDate.plusMonths(i).withDayOfMonth(1).toLocalDate();
            DailyTicketRevenueResponse resp = map.getOrDefault(
                    monthStart,
                    new DailyTicketRevenueResponse(monthStart, 0L, 0L));
            result.add(resp);
        }
        return result;
    }

    public List<BookingTicketRecentlyResponse> getRecentlyBookedTickets(int limit) {
        List<Object[]> stats = bookingRepository.getBookingTicketRecently(limit);
        List<BookingTicketRecentlyResponse> result = new ArrayList<>();
        for (Object[] row : stats) {
            String fullName = (String) row[1];
            String email = (String) row[2];
            String movieTitle = (String) row[3];
            String cinemaRoomName = (String) row[4];
            LocalDateTime bookingDate = ((java.sql.Timestamp) row[5]).toLocalDateTime();
            int seatCount = ((Number) row[6]).intValue();
            long totalPrice = ((Number) row[7]).longValue();
            String paymentMethod = (String) row[8];
            String paymentStatus = (String) row[9];

            result.add(new BookingTicketRecentlyResponse(
                    fullName,
                    email,
                    movieTitle,
                    cinemaRoomName,
                    bookingDate,
                    seatCount,
                    totalPrice,
                    paymentMethod,
                    paymentStatus));
        }
        return result;
    }

}
