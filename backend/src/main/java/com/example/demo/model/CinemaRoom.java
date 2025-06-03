package com.example.demo.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "cinema_room")
public class CinemaRoom {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cinema_room_id")
    private Long id;

    @Column(name = "cinema_room_name")
    private String name;

    @Column(name = "seat_quantity")
    private Integer seatQuantity;


}