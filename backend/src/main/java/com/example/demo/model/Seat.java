package com.example.demo.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "SEAT")
public class Seat {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "SEAT_ID")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "CINEMA_ROOM_ID")
    private CinemaRoom cinemaRoom;

    @Column(name = "SEAT_ROW")
    private String seatRow;

    @Column(name = "SEAT_COL")
    private String seatCol;

    @Column(name = "SEAT_STATUS")
    private String seatStatus;

    @Column(name = "SEAT_TYPE")
    private String seatType;


}
