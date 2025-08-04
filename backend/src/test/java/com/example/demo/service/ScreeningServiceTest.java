package com.example.demo.service;

import com.example.demo.DTO.request.ScreeningRequest;
import com.example.demo.exception.AppException;
import com.example.demo.exception.NotFoundException;
import com.example.demo.model.*;
import com.example.demo.repository.*;
import com.example.demo.utils.SecurityUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ScreeningServiceTest {

    @InjectMocks
    private ScreeningService screeningService;

    @Mock private ScreeningRepository screeningRepository;
    @Mock private MovieRepository movieRepository;
    @Mock private CinemaRoomRepository cinemaRoomRepository;
    @Mock private FareTypeRepository fareTypeRepository;
    @Mock private AccountRepository accountRepository;
    @Mock private ActivityLogService activityLogService;
    @Mock private NotificationService notificationService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }



    @Test
    void testAddScreening_MovieNotFound() {
        ScreeningRequest request = new ScreeningRequest();
        request.setMovieId(404L);
        when(movieRepository.findById(404L)).thenReturn(Optional.empty());

        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> screeningService.addScreening(request));
        assertTrue(ex.getMessage().contains("Không tìm thấy phim"));
    }

    @Test
    void testAddScreening_Overlap() {
        ScreeningRequest request = new ScreeningRequest();
        request.setMovieId(1L);
        request.setCinemaRoomId(10L);
        request.setShowDateTime(LocalDateTime.now());

        Movie movie = new Movie();
        movie.setDuration(100);

        when(movieRepository.findById(1L)).thenReturn(Optional.of(movie));
        when(screeningRepository.findOverlappingScreenings(anyLong(), any(), any()))
                .thenReturn(List.of(new Screening()));

        AppException ex = assertThrows(AppException.class,
                () -> screeningService.addScreening(request));
        assertTrue(ex.getMessage().contains("Lịch chiếu bị trùng"));
    }

    @Test
    void testAddScreening_CinemaRoomNotFound() {
        ScreeningRequest request = new ScreeningRequest();
        request.setMovieId(1L);
        request.setCinemaRoomId(99L);
        request.setShowDateTime(LocalDateTime.now());

        Movie movie = new Movie();
        movie.setDuration(90);

        when(movieRepository.findById(1L)).thenReturn(Optional.of(movie));
        when(screeningRepository.findOverlappingScreenings(anyLong(), any(), any()))
                .thenReturn(Collections.emptyList());
        when(cinemaRoomRepository.findById(99L)).thenReturn(Optional.empty());

        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> screeningService.addScreening(request));
        assertTrue(ex.getMessage().contains("Không tìm thấy phòng chiếu"));
    }

    @Test
    void testAddScreening_FareTypeNotFound() {
        ScreeningRequest request = new ScreeningRequest();
        request.setMovieId(1L);
        request.setCinemaRoomId(10L);
        request.setFareTypeId(500L);
        request.setShowDateTime(LocalDateTime.now());

        Movie movie = new Movie();
        movie.setDuration(90);
        CinemaRoom room = new CinemaRoom();

        when(movieRepository.findById(1L)).thenReturn(Optional.of(movie));
        when(screeningRepository.findOverlappingScreenings(anyLong(), any(), any()))
                .thenReturn(Collections.emptyList());
        when(cinemaRoomRepository.findById(10L)).thenReturn(Optional.of(room));
        when(fareTypeRepository.findById(500L)).thenReturn(Optional.empty());

        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> screeningService.addScreening(request));
        assertTrue(ex.getMessage().contains("Không tìm thấy loại vé"));
    }
}
