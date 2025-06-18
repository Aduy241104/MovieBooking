package com.example.demo.service;

import com.example.demo.DTO.response.dashboard.DailyTicketRevenueResponse;
import com.example.demo.repository.BookingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BookingService {
    private final BookingRepository bookingRepository;

    public Long getTotalRevenueByStatus(String status) {
        return bookingRepository.getTotalRevenueByStatus(status);
    }

    public List<DailyTicketRevenueResponse> getDailyTicketRevenue(int dailyCount) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime fromDate = now.minusDays(dailyCount - 1).toLocalDate().atStartOfDay();
        LocalDateTime toDate = now.plusDays(1).toLocalDate().atStartOfDay();

        List<Object[]> stats = bookingRepository.getDailyTicketRevenue(fromDate, toDate);

        List<DailyTicketRevenueResponse> result = new ArrayList<>();
        for (Object[] row : stats) {
            LocalDate date = ((java.sql.Timestamp) row[0]).toLocalDateTime().toLocalDate();
            Long revenue = ((Number) row[1]).longValue();
            Long tickets = ((Number) row[2]).longValue();
            result.add(new DailyTicketRevenueResponse(date, revenue, tickets));
        }
        return result;
    }

    public List<DailyTicketRevenueResponse> getWeeklyTicketRevenue(int weekCount) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime fromDate = now.minusWeeks(weekCount - 1).with(java.time.DayOfWeek.MONDAY).toLocalDate().atStartOfDay();
        LocalDateTime toDate = now.plusDays(1).toLocalDate().atStartOfDay();

        List<Object[]> stats = bookingRepository.getWeeklyRevenueAndTickets(fromDate, toDate);

        List<DailyTicketRevenueResponse> result = new ArrayList<>();
        for (Object[] row : stats) {
            LocalDate weekStart = ((java.sql.Timestamp) row[0]).toLocalDateTime().toLocalDate();
            Long revenue = ((Number) row[1]).longValue();
            Long tickets = ((Number) row[2]).longValue();
            result.add(new DailyTicketRevenueResponse(weekStart, revenue, tickets));
        }
        return result;
    }

    public List<DailyTicketRevenueResponse> getMonthlyTicketRevenue(int monthCount) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime fromDate = now.minusMonths(monthCount - 1).withDayOfMonth(1).toLocalDate().atStartOfDay();
        LocalDateTime toDate = now.plusDays(1).toLocalDate().atStartOfDay();

        List<Object[]> stats = bookingRepository.getMonthlyRevenueAndTickets(fromDate, toDate);

        List<DailyTicketRevenueResponse> result = new ArrayList<>();
        for (Object[] row : stats) {
            LocalDate monthStart = ((java.sql.Timestamp) row[0]).toLocalDateTime().toLocalDate();
            Long revenue = ((Number) row[1]).longValue();
            Long tickets = ((Number) row[2]).longValue();
            result.add(new DailyTicketRevenueResponse(monthStart, revenue, tickets));
        }
        return result;
    }

}
