package com.example.demo.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "screening")
public class Screening {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "screening_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "movie_id")
    private Movie movie;

    @ManyToOne
    @JoinColumn(name = "cinema_room_id")
    private CinemaRoom cinemaRoom;

    @ManyToOne
    @JoinColumn(name = "fare_type_id")
    private FareType fareType;

    @Column(name = "show_date_time")
    private LocalDateTime showDateTime;
 
    @Column(name = "is_deleted")
    private Boolean isDeleted = false;

    // Calculate end time: showDateTime + movie duration (minutes)
    public LocalDateTime getEndDateTime() {
        if (movie != null && movie.getDuration() != null) {
            return showDateTime.plusMinutes(movie.getDuration());
        }
        return showDateTime; // Fallback if no duration
    }
}