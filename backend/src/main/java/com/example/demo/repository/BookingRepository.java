package com.example.demo.repository;

import com.example.demo.model.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    @Query(value = """
            SELECT COALESCE(SUM(bs.price_paid), 0)
            FROM booked_seat bs
            JOIN booking b ON bs.booking_id = b.booking_id
            WHERE b.booking_status = :status
            """, nativeQuery = true)
    Long getTotalRevenueByStatus(String status);

    @Query(value = """
            SELECT
                DATE_TRUNC('day', b.booking_time) as daily,
                COALESCE(SUM(bs.price_paid), 0) AS revenue,
                COUNT(bs.booked_seat_id) as tickets
            FROM booked_seat bs
            JOIN booking b ON bs.booking_id = b.booking_id
            WHERE b.booking_status = 'PAID'
                AND b.booking_time >= :fromDate
                AND b.booking_time < :toDate
            GROUP BY daily
            ORDER BY daily
            """, nativeQuery = true)
    List<Object[]> getDailyTicketRevenue(LocalDateTime fromDate, LocalDateTime toDate);

    @Query(value = """
            SELECT
                DATE_TRUNC('week', b.booking_time) AS week_start,
                COALESCE(SUM(bs.price_paid), 0) AS revenue,
                COUNT(bs.booked_seat_id) AS tickets
            FROM booking b
            JOIN booked_seat bs ON b.booking_id = bs.booking_id
            WHERE b.booking_status = 'PAID'
                AND b.booking_time >= :fromDate
                AND b.booking_time < :toDate
            GROUP BY week_start
            ORDER BY week_start
            """, nativeQuery = true)
    List<Object[]> getWeeklyRevenueAndTickets(LocalDateTime fromDate, LocalDateTime toDate);

    @Query(value = """
            SELECT
                DATE_TRUNC('month', b.booking_time) AS month_start,
                COALESCE(SUM(bs.price_paid), 0) AS revenue,
                COUNT(bs.booked_seat_id) AS tickets
            FROM booking b
            JOIN booked_seat bs ON b.booking_id = bs.booking_id
            WHERE b.booking_status = 'PAID'
                AND b.booking_time >= :fromDate
                AND b.booking_time < :toDate
            GROUP BY month_start
            ORDER BY month_start
            """, nativeQuery = true)
    List<Object[]> getMonthlyRevenueAndTickets(LocalDateTime fromDate, LocalDateTime toDate);
}
