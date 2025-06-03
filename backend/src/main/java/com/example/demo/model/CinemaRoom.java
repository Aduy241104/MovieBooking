package com.example.demo.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "CINEMA_ROOM")
public class CinemaRoom {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "CINEMA_ROOM_ID")
    private Long id;

    @Column(name = "CINEMA_ROOM_NAME")
    private String name;

    @Column(name = "SEAT_QUANTITY")
    private Integer seatQuantity;


}