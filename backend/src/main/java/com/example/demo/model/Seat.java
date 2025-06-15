package com.example.demo.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "seat", uniqueConstraints = @UniqueConstraint(columnNames = { "cinema_room_id", "seat_row", "seat_col" }))
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Seat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "seat_id")
    private Long seatId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cinema_room_id", nullable = false)
    private CinemaRoom cinemaRoom;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seat_type_id", nullable = true)
    private SeatType seatType;

    @Column(name = "seat_col", length = 255)
    private String seatCol;

    @Column(name = "seat_row")
    private String seatRow;

    @Column(name = "seat_status")
    private String seatStatus;
}
