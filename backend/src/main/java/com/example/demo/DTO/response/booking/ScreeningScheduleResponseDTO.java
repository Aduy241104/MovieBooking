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
    private Movie movie; // Thông tin cơ bản của phim
    private Map<LocalDate, List<ScreeningTimeDTO>> schedules; // Key: Ngày chiếu, Value: List các suất chiếu trong ngày đó

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ScreeningTimeDTO {
        private Long screeningId;
        private LocalTime time;
        private CinemaRoom cinemaRoom; // Thêm thông tin phòng chiếu
        private FareType fareType; // Thêm thông tin loại giá vé
        private String movieFormat; // VD: 2D, 3D
    }
}