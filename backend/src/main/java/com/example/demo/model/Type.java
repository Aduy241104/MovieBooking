package com.example.demo.model;


import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "type")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Type {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "type_id")
    private Long id;

    @Column(name = "type_name", unique = true, nullable = false)
    private String name;

    @Column(name = "is_deleted")
    private Boolean isDeleted = false;
}