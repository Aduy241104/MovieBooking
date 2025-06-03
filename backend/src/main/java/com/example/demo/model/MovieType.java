package com.example.demo.model;


import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "MOVIE_TYPE", uniqueConstraints = @UniqueConstraint(columnNames = {"MOVIE_ID", "TYPE_ID"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class MovieType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MOVIE_TYPE_ID")
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "MOVIE_ID", nullable = false)
    private Movie movie;

    @ManyToOne
    @JoinColumn(name = "TYPE_ID", nullable = false)
    private Type type;
}