package com.example.demo.DTO.response.booking;
import com.example.demo.model.CinemaRoom;
import com.example.demo.model.FareType;
import com.example.demo.model.Movie;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScreeningScheduleResponseDTO {
    private Movie movie;
    private Map<LocalDate, List<ScreeningTimeDTO>> schedules;
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ScreeningTimeDTO {
        private Long screeningId;
        private LocalTime time;
        private CinemaRoom cinemaRoom;
        private FareType fareType;
        private String movieFormat;
    }
}