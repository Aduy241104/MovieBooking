package com.example.demo.DTO.request;

import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class MovieRequest {
    private String nameVN;
    private String nameEN;
    private Integer duration;
    private String content;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate fromDate;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate toDate;

    private MultipartFile smallImage;
    private MultipartFile largeImage;
    private String trailerLink;
    private String director;
    private String actor;
    private String movieProductionCompany;
    private Integer ageLimit;
    private List<Integer> typeIds;
}
