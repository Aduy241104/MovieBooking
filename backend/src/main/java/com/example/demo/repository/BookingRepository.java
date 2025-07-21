package com.example.demo.repository;

import com.example.demo.DTO.response.SingleMovieDTO;
import com.example.demo.model.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

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

    @Query(value = """
            SELECT
                b.booking_id AS bookingId,
                a.full_name AS customerName,
                a.email AS customerEmail,
                m.movie_name_en AS movieTitle,
                r.cinema_room_name AS cinemaRoom,
                b.booking_time AS bookingDate,
                (SELECT COUNT(*) FROM booked_seat bs WHERE bs.booking_id = b.booking_id) AS seatCount,
                b.total_amount AS totalAmount,
                p.method_name AS paymentMethod,
                b.booking_status AS status
            FROM booking b
            JOIN account a ON b.account_id = a.account_id
            JOIN screening s ON b.screening_id = s.screening_id
            JOIN movie m ON s.movie_id = m.movie_id
            JOIN cinema_room r ON s.cinema_room_id = r.cinema_room_id
            JOIN payment_method p ON b.payment_method_id = p.payment_method_id
            ORDER BY b.booking_time DESC
            LIMIT ?1
            """, nativeQuery = true)
    List<Object[]> getBookingTicketRecently(int limit);


    @Query("SELECT COUNT(b) FROM Booking b " +
            "JOIN Screening s ON b.screening.id = s.id " +
            "JOIN Movie m ON s.movie.id = m.id " +
            "WHERE b.account.id = :accountId AND m.id = :movieId AND b.bookingStatus = 'PAID'")
    long countPaidBookings(@Param("accountId") Long accountId, @Param("movieId") Long movieId);


    // Booking
    List<Booking> findByAccountAccountIdOrderByBookingTimeDesc(Long accountId);

    Optional<Booking> findByIdAndAccountAccountId(Integer bookingId, Long accountId);

    Optional<Booking> findByVnpTxnRef(String vnpTxnRef); // Thêm findByVnpTxnRef

    @Query("""
                SELECT new com.example.demo.DTO.response.SingleMovieDTO(
                    m.id,
                    m.nameVN,
                    m.nameEN,
                    m.duration,
                    m.content,
                    m.fromDate,
                    m.toDate,
                    m.smallImage,
                    m.largeImage,
                    m.trailer,
                    m.ageLimit,
                    m.director,
                    m.movieProductionCompany,
                    null,
                    null
                )
                FROM Booking b
                JOIN b.screening s
                JOIN s.movie m
                WHERE :currentDate BETWEEN m.fromDate AND m.toDate
                  AND m.isDeleted = false
                GROUP BY m.id, m.nameVN, m.nameEN, m.duration, m.content, m.fromDate, m.toDate,
                         m.smallImage, m.largeImage, m.trailer, m.ageLimit,
                         m.director, m.movieProductionCompany
                ORDER BY COUNT(b.id) DESC
            """)
    List<SingleMovieDTO> getTopBookedCurrentMovies(@Param("currentDate") LocalDate currentDate);

    boolean existsByBookingCode(String bookingCode); // code



   //Tìm booking đang chờ thanh toán của một user cho một suất chiếu cụ thể.

    Optional<Booking> findByAccountAccountIdAndScreeningIdAndBookingStatus(Long accountId, Long screeningId, String status);

    /**
     * Tìm tất cả các booking đang chờ và được tạo trước một thời điểm nhất định (đã hết hạn).
     */
    List<Booking> findAllByBookingStatusAndBookingTimeBefore(String status, LocalDateTime expirationTime);

}

