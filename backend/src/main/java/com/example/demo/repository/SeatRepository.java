package com.example.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.model.CinemaRoom;
import com.example.demo.model.Seat;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Set;

public interface SeatRepository extends JpaRepository<Seat, Long> {
    List<Seat> findByCinemaRoom(CinemaRoom room);

    // Lấy danh sách các ghế đã được đặt cho một suất chiếu cụ thể
    @Query("SELECT bs.seat.seatId FROM BookedSeat bs WHERE bs.booking.screening.id = :screeningId AND bs.booking.bookingStatus IN ('PAID', 'PENDING_PAYMENT', 'RESERVED')")
    Set<Long> findBookedSeatIdsByScreeningId(@Param("screeningId") Long screeningId);

    List<Seat> findBySeatIdIn(List<Long> seatIds);
}