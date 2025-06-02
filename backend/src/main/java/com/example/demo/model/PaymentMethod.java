package com.example.demo.model;


import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "PAYMENT_METHOD")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class PaymentMethod {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PAYMENT_METHOD_ID")
    private Long id;

    @Column(name = "METHOD_NAME")
    private String name;

    @Column(name = "DESCRIPTION")
    private String description;

    @Column(name = "IS_ACTIVE")
    private Boolean active;
}