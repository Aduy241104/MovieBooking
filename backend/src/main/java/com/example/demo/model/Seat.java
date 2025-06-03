package com.example.demo.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "seat")
public class Seat {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "seat_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "cinema_room_id")
    private CinemaRoom cinemaRoom;

    @Column(name = "seat_row")
    private String seatRow;

    @Column(name = "seat_col")
    private String seatCol;

    @Column(name = "seat_status")
    private String seatStatus;

    @Column(name = "seat_type")
    private String seatType;


}
