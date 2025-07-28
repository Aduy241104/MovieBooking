package com.example.demo.repository;

import com.example.demo.DTO.response.SingleMovieDTO;
import com.example.demo.DTO.response.dashboard.BookingTicketRecentlyResponse;
import com.example.demo.DTO.response.dashboard.DailyTicketRevenueResponse;
import com.example.demo.model.Booking;
import org.springframework.data.domain.Pageable;
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
            SELECT COALESCE(SUM(b.total_amount), 0)
            FROM booking b
            WHERE b.booking_status = :status
            """, nativeQuery = true)
    Optional<Long> getTotalRevenueByStatus(String status);

    @Query(value = """
            SELECT
                DATE_TRUNC('day', b.booking_time) as date,
                COALESCE(SUM(bs.price_paid), 0) AS revenue,
                COUNT(bs.booked_seat_id) as tickets
            FROM booked_seat bs
            JOIN booking b ON bs.booking_id = b.booking_id
            WHERE b.booking_status = 'PAID'
                AND b.booking_time >= :fromDate
                AND b.booking_time < :toDate
            GROUP BY date
            ORDER BY date
            """, nativeQuery = true)
    List<DailyTicketRevenueResponse> getDailyTicketRevenue(@Param("fromDate") LocalDateTime fromDate,
            @Param("toDate") LocalDateTime toDate);

    @Query(value = """
            SELECT
                DATE_TRUNC('week', b.booking_time) AS date,
                COALESCE(SUM(bs.price_paid), 0) AS revenue,
                COUNT(bs.booked_seat_id) AS tickets
            FROM booking b
            JOIN booked_seat bs ON b.booking_id = bs.booking_id
            WHERE b.booking_status = 'PAID'
                AND b.booking_time >= :fromDate
                AND b.booking_time < :toDate
            GROUP BY date
            ORDER BY date
            """, nativeQuery = true)
    List<DailyTicketRevenueResponse> getWeeklyRevenueAndTickets(@Param("fromDate") LocalDateTime fromDate,
            @Param("toDate") LocalDateTime toDate);

    @Query(value = """
            SELECT
                DATE_TRUNC('month', b.booking_time) AS date,
                COALESCE(SUM(bs.price_paid), 0) AS revenue,
                COUNT(bs.booked_seat_id) AS tickets
            FROM booking b
            JOIN booked_seat bs ON b.booking_id = bs.booking_id
            WHERE b.booking_status = 'PAID'
                AND b.booking_time >= :fromDate
                AND b.booking_time < :toDate
            GROUP BY date
            ORDER BY date
            """, nativeQuery = true)
    List<DailyTicketRevenueResponse> getMonthlyRevenueAndTickets(@Param("fromDate") LocalDateTime fromDate,
            @Param("toDate") LocalDateTime toDate);

    @Query("""
                SELECT new com.example.demo.DTO.response.dashboard.BookingTicketRecentlyResponse(
                    b.id,
                    b.account.fullName,
                    b.account.email,
                    b.screening.movie.nameEN,
                    b.screening.cinemaRoom.cinemaRoomName,
                    b.bookingTime,
                    COUNT(bs.id),
                    b.totalAmount,
                    b.paymentMethod.name,
                    b.bookingStatus
                )
                FROM Booking b
                JOIN b.bookedSeats bs
                GROUP BY b.id, b.account.fullName, b.account.email, b.screening.movie.nameEN,
                         b.screening.cinemaRoom.cinemaRoomName, b.bookingTime, b.totalAmount,
                         b.paymentMethod.name, b.bookingStatus
                ORDER BY b.bookingTime DESC
                LIMIT ?1
            """)
    List<BookingTicketRecentlyResponse> getBookingTicketRecently(int limit);

    // Booking
    List<Booking> findByAccountAccountIdOrderByBookingTimeDesc(Long accountId);
    Optional<Booking> findByIdAndAccountAccountId(Integer bookingId, Long accountId);
    Optional<Booking> findByVnpTxnRef(String vnpTxnRef);
    boolean existsByBookingCode(String bookingCode);
    //Find a user's pending booking for a specific showtime.
    Optional<Booking> findByAccountAccountIdAndScreeningIdAndBookingStatus(Long accountId, Long screeningId, String status);

    //Find all bookings that are pending and created before a certain time (expired).
    List<Booking> findAllByBookingStatusAndBookingTimeBefore(String status, LocalDateTime expirationTime);

    @Query("""
            SELECT b
            FROM Booking b
            JOIN b.screening s
            WHERE s.movie.id = :movieId
            ORDER BY b.bookingTime DESC
            """)
    List<Booking> findAllByMovieId(@Param("movieId") Long movieId);

    @Query("""
            SELECT COUNT(b)
            FROM Booking b
            JOIN b.screening s
            WHERE s.movie.id = :movieId
            """)
    Long countBookingsByMovieId(@Param("movieId") Long movieId);

    List<Booking> findByBookingStatusInAndBookingTimeBetweenOrderByBookingTimeAsc(
            List<String> statuses,
            LocalDateTime startTime,
            LocalDateTime endTime
    );
    //Booking-end
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





}