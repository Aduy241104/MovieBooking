package com.example.demo.DTO.response;

import java.time.LocalDate;
import java.util.List;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SingleMovieDTO {
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
    private Double avgRating;
    private List<String> types; // Thêm danh sách thể loại
}
