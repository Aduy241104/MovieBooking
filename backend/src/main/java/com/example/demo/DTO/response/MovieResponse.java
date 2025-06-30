package com.example.demo.DTO.response;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class MovieResponse {
    private Long id;
    private String nameVN;
    private String nameEN;
    private Integer duration;
    private String content;
    private LocalDate fromDate;
    private LocalDate toDate;
    private String smallImage;
    private String largeImage;
    private String trailer;
    private String director;
    private String movieProductionCompany;
    private String actor;
    private Integer ageLimit;
}