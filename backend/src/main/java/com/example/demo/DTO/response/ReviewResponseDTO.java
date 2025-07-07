package com.example.demo.DTO.response;

import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReviewResponseDTO {
    private Long id;
    private Long accountId;
    private String avatar;
    private String accountFullName;
    private Integer rating;
    private String comment;
    private LocalDateTime reviewDate;
    private Boolean approved;
    private Boolean spoilerAlert;
}
