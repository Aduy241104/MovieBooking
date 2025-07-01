package com.example.demo.DTO.response;

import lombok.*;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MovieResponse {
    private Long id;
    private String nameVN;
    private String nameEN;
    private Integer duration;
    private String content;
    private LocalDate fromDate;
    private LocalDate toDate;
    private String smallImageUrl;
    private String largeImageUrl;
    private String trailerUrl;
    private String director;
    private String actor;
    private String movieProductionCompany;
    private Integer ageLimit;
    private List<String> typeNames;

    private List<Long> typeIds;
}

