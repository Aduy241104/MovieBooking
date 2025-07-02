package com.example.demo.repository;

import com.example.demo.model.CinemaRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatbotCinemaRepository extends JpaRepository<CinemaRoom, Long> {

    // Thông tin tất cả phòng chiếu
    List<CinemaRoom> findAllByIsDeletedFalse();

    // Phòng VIP
    @Query("SELECT c FROM CinemaRoom c WHERE LOWER(c.cinemaRoomName) LIKE '%vip%' AND c.isDeleted = false")
    List<CinemaRoom> findVipRooms();

    // Phòng có hỗ trợ 3D
    @Query("SELECT c FROM CinemaRoom c WHERE LOWER(c.cinemaRoomName) LIKE '%3d%' AND c.isDeleted = false")
    List<CinemaRoom> find3DRooms();

    // Phòng IMAX (nếu có)
    @Query("SELECT c FROM CinemaRoom c WHERE LOWER(c.cinemaRoomName) LIKE '%imax%' AND c.isDeleted = false")
    List<CinemaRoom> findIMAXRooms();

    // Tổng số ghế của tất cả phòng
    @Query("SELECT SUM(c.seatQuantity) FROM CinemaRoom c WHERE c.isDeleted = false")
    Integer getTotalSeatsCount();

    // Phòng có số ghế lớn nhất
    @Query("SELECT c FROM CinemaRoom c WHERE c.isDeleted = false ORDER BY c.seatQuantity DESC")
    List<CinemaRoom> findRoomsOrderByCapacity();
}
