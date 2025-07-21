package com.example.demo.DTO.request;

import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReviewRequestDTO {
    private Long movieId;
    private Long accountId;
    private Integer rating;
    private String comment;
    private Boolean approved;
    private Boolean spoilerAlert;
    private LocalDateTime reviewDate;
}
