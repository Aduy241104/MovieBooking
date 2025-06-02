package com.example.demo.model;



import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
@Getter
@Setter
@Entity
@Table(name = "PAYMENT")
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PAYMENT_ID")
    private Long id;

    @OneToOne
    @JoinColumn(name = "BOOKING_ID")
    private Booking booking;

    @Column(name = "PAYMENT_METHOD_ID")
    private Long paymentMethodId;

    @Column(name = "AMOUNT_PAID")
    private BigDecimal amountPaid;

    @Column(name = "TRANSACTION_DATE")
    private LocalDateTime transactionDate;

    @Column(name = "PAYMENT_STATUS")
    private String paymentStatus;


}