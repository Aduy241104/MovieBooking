package com.example.demo.model;


import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "booking")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "booking_id")
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "account_id")
    private Account account;

    @ManyToOne
    @JoinColumn(name = "screening_id", nullable = false)
    private Screening screening;

    @ManyToOne
    @JoinColumn(name = "promotion_id")
    private Promotion promotion;

    @Column(name = "promotion_code_applied")
    private String promotionCodeApplied;

    @Column(name = "discount_type_applied")
    private String discountTypeApplied;

    @Column(name = "discount_applied")
    private BigDecimal discountApplied;

    @Column(name = "booking_time")
    private LocalDateTime bookingTime;

    @Column(name = "total_amount")
    private BigDecimal totalAmount;

    @Column(name = "booking_status")
    private String bookingStatus;

    @OneToMany(mappedBy = "booking", cascade = CascadeType.ALL)
    private List<BookedSeat> bookedSeats = new ArrayList<>();
}