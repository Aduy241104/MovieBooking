package com.example.demo.model;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
@Getter
@Setter
@Entity
@Table(name = "promotion")
public class Promotion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "promotion_id")
    private Long id;

    @Column(name = "discount_type")
    private String discountType;

    @Column(name = "discount_level")
    private BigDecimal discountLevel;

    @Column(name = "code")
    private String code;

    @Column(name = "max_discount")
    private BigDecimal maxDiscount;

    @Column(name = "min_order")
    private BigDecimal minOrder;

    @Column(name = "start_time")
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Column(name = "promotion_active")
    private Boolean active;


}
