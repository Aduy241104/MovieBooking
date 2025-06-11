package com.example.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.model.SeatType;

import java.util.Optional;

public interface SeatTypeRepository extends JpaRepository<SeatType, Long> {
    Optional<SeatType> findBySeatTypeNameIgnoreCase(String seatTypeName);
}

