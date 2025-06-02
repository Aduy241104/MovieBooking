package com.example.demo.model;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
@Getter
@Setter
@Entity
@Table(name = "PROMOTION")
public class Promotion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PROMOTION_ID")
    private Long id;

    @Column(name = "DISCOUNT_TYPE")
    private String discountType;

    @Column(name = "DISCOUNT_LEVEL")
    private BigDecimal discountLevel;

    @Column(name = "CODE")
    private String code;

    @Column(name = "MAX_DISCOUNT")
    private BigDecimal maxDiscount;

    @Column(name = "MIN_ORDER")
    private BigDecimal minOrder;

    @Column(name = "START_TIME")
    private LocalDateTime startTime;

    @Column(name = "END_TIME")
    private LocalDateTime endTime;

    @Column(name = "PROMOTION_ACTIVE")
    private Boolean active;


}
