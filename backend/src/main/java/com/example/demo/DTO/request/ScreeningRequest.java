package com.example.demo.DTO.request;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class ScreeningRequest {
    private Long movieId;
    private Long cinemaRoomId;
    private Long fareTypeId;
    private LocalDateTime showDateTime;
}
