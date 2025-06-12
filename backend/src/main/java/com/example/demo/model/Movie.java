package com.example.demo.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(name = "movie")
public class Movie {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "movie_id")
    private Long id;

    @Column(name = "movie_name_vn")
    private String nameVN;

    @Column(name = "movie_name_en")
    private String nameEN;

    @Column(name = "duration")
    private Integer duration;

    // Chỉnh sửa ở đây: dùng TEXT thay vì VARCHAR(255)
    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    @Column(name = "from_date")
    private LocalDate fromDate;

    @Column(name = "to_date")
    private LocalDate toDate;

    @Column(name = "small_image")
    private String smallImage;

    @Column(name = "large_image")
    private String largeImage;

    @Column(name = "trailer")
    private String trailer;

    @Column(name = "director")
    private String director;

    @Column(name = "movie_production_company")
    private String movieProductionCompany;

    @Column(name = "actor")
    private String actor;

    @Column(name = "age_limit")
    private Integer ageLimit;
}
