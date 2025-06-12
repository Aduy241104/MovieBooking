package com.example.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.model.CinemaRoom;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CinemaRoomRepository extends JpaRepository<CinemaRoom, Long> {
    Optional<CinemaRoom> findByCinemaRoomNameIgnoreCase(String name);

    boolean existsByCinemaRoomNameIgnoreCase(String name);

}
