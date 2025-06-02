package com.example.demo.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
@Getter
@Setter
@Entity
@Table(name = "MOVIE")
public class Movie {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MOVIE_ID")
    private Long id;

    @Column(name = "MOVIE_NAME_VN")
    private String nameVN;

    @Column(name = "MOVIE_NAME_EN")
    private String nameEN;

    @Column(name = "DURATION")
    private Integer duration;

    @Column(name = "CONTENT")
    private String content;

    @Column(name = "FROM_DATE")
    private LocalDate fromDate;

    @Column(name = "TO_DATE")
    private LocalDate toDate;

    @Column(name = "SMALL_IMAGE")
    private String smallImage;

    @Column(name = "LARGE_IMAGE")
    private String largeImage;

    @Column(name = "TRAILER")
    private String trailer;


}