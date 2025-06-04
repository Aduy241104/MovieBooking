package com.example.demo.model;



import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
@Getter
@Setter
@Entity
@Table(name = "payment")
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "payment_id")
    private Long id;

    @OneToOne
    @JoinColumn(name = "booking_id")
    private Booking booking;

    @Column(name = "payment_method_id")
    private Long paymentMethodId;

    @Column(name = "amount_paid")
    private BigDecimal amountPaid;

    @Column(name = "transaction_date")
    private LocalDateTime transactionDate;

    @Column(name = "payment_status")
    private String paymentStatus;


}