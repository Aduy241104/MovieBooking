package com.example.demo.model;


import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "TYPE")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Type {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "TYPE_ID")
    private Integer id;

    @Column(name = "TYPE_NAME", unique = true, nullable = false)
    private String name;
}