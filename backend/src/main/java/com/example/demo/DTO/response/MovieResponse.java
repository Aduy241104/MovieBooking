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
    private String director;
    private String actor;
    private Integer ageLimit;
}