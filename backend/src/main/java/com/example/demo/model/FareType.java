package com.example.demo.model;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
@Getter
@Setter
@Entity
@Table(name = "fare_type")
public class FareType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "fare_type_id")
    private Long id;

    @Column(name = "fare_type_name")
    private String name;

    @Column(name = "price")
    private BigDecimal price;


}