package com.example.demo.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
@Getter
@Setter
@Entity
@Table(name = "BOOKED_SEAT")
public class BookedSeat {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "BOOKED_SEAT_ID")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "BOOKING_ID")
    private Booking booking;

    @ManyToOne
    @JoinColumn(name = "SEAT_ID")
    private Seat seat;

    @Column(name = "PRICE_PAID")
    private BigDecimal pricePaid;


}