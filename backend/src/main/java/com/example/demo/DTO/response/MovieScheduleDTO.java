package com.example.demo.DTO.response;

import java.util.List;

import com.example.demo.model.Movie;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MovieScheduleDTO {
    private Movie movie;
    private List<String> types;
    private List<ShowTimeDTO> showTime;
}
