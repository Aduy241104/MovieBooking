package com.example.demo.service;

import com.example.demo.repository.BookedSeatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BookedSeatService {
    private final BookedSeatRepository bookedSeatRepository;

    public Long totalBookedSeats() {
        return bookedSeatRepository.count();
    }
}
