package com.example.demo.DTO.response;

import java.time.LocalTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ShowTimeDTO {
    private Long screeningId;
    private LocalTime showTime;
    private LocalTime endTime;
}
