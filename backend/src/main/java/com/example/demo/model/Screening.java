package com.example.demo.model;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "SCREENING")
public class Screening {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "SCREENING_ID")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "MOVIE_ID")
    private Movie movie;

    @ManyToOne
    @JoinColumn(name = "CINEMA_ROOM_ID")
    private CinemaRoom cinemaRoom;

    @ManyToOne
    @JoinColumn(name = "FARE_TYPE_ID")
    private FareType fareType;

    @Column(name = "SHOW_DATE_TIME")
    private LocalDateTime showDateTime;


}