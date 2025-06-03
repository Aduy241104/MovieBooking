package com.example.demo.model;


import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "BOOKING")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "BOOKING_ID")
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "ACCOUNT_ID")
    private Account account;

    @ManyToOne
    @JoinColumn(name = "SCREENING_ID", nullable = false)
    private Screening screening;

    @ManyToOne
    @JoinColumn(name = "PROMOTION_ID")
    private Promotion promotion;

    @Column(name = "PROMOTION_CODE_APPLIED")
    private String promotionCodeApplied;

    @Column(name = "DISCOUNT_TYPE_APPLIED")
    private String discountTypeApplied;

    @Column(name = "DISCOUNT_APPLIED")
    private BigDecimal discountApplied;

    @Column(name = "BOOKING_TIME")
    private LocalDateTime bookingTime;

    @Column(name = "TOTAL_AMOUNT")
    private BigDecimal totalAmount;

    @Column(name = "BOOKING_STATUS")
    private String bookingStatus;

    @OneToMany(mappedBy = "booking", cascade = CascadeType.ALL)
    private List<BookedSeat> bookedSeats = new ArrayList<>();
}