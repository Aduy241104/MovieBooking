package com.example.demo.DTO.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MovieReviewWrapperDTO {
    private Long movieid; // movieId
    private String movieName;
    private List<ReviewResponseDTO> reviews;
}
