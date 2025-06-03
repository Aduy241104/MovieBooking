package com.example.demo.model;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
@Getter
@Setter
@Entity
@Table(name = "FARE_TYPE")
public class FareType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "FARE_TYPE_ID")
    private Long id;

    @Column(name = "FARE_TYPE_NAME")
    private String name;

    @Column(name = "PRICE")
    private BigDecimal price;


}