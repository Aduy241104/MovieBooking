package com.example.demo.service;

import com.example.demo.DTO.request.MovieRequest;
import com.example.demo.DTO.response.MovieResponse;
import com.example.demo.exception.DuplicateNameException;
import com.example.demo.exception.NotFoundException;
import com.example.demo.model.*;
import com.example.demo.repository.*;
import com.example.demo.utils.SecurityUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.ByteArrayInputStream;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MovieServiceTest {

    @InjectMocks
    private MovieService movieService;

    @Mock
    private MovieRepository movieRepository;
    @Mock
    private TypeRepository typeRepository;
    @Mock
    private MovieTypeRepository movieTypeRepository;
    @Mock
    private AccountRepository accountRepository;
    @Mock
    private ActivityLogService activityLogService;
    @Mock
    private NotificationService notificationService;

    @Mock
    private MultipartFile smallImage;
    @Mock
    private MultipartFile largeImage;

    private Movie movie;
    private MovieRequest request;

    @BeforeEach
    void setup() {
        movie = new Movie();
        movie.setId(1L);
        movie.setNameVN("Tên VN");
        movie.setNameEN("Tên EN");

        request = new MovieRequest();
        request.setNameVN("Tên VN");
        request.setNameEN("Tên EN");
        request.setSmallImage(smallImage);
        request.setLargeImage(largeImage);
        request.setTrailerLink("https://youtube.com/trailer");
        request.setTypeIds(List.of(1L));
    }

    @Test
    void createMovie_shouldSucceed() throws Exception {
        when(movieRepository.findByNameVNAndIsDeletedFalse("Tên VN")).thenReturn(Optional.empty());
        when(movieRepository.findByNameENAndIsDeletedFalse("Tên EN")).thenReturn(Optional.empty());
        when(typeRepository.findAllByIdIn(List.of(1L)))
                .thenReturn(List.of(new Type(1L, "Hành động", false)));

        when(movieRepository.save(any(Movie.class))).thenAnswer(invocation -> {
            Movie m = invocation.getArgument(0);
            m.setId(1L);
            return m;
        });

        when(smallImage.isEmpty()).thenReturn(false);
        when(largeImage.isEmpty()).thenReturn(false);
        when(smallImage.getOriginalFilename()).thenReturn("small.jpg");
        when(largeImage.getOriginalFilename()).thenReturn("large.jpg");
        when(smallImage.getInputStream()).thenReturn(new ByteArrayInputStream("dummy".getBytes()));
        when(largeImage.getInputStream()).thenReturn(new ByteArrayInputStream("dummy".getBytes()));

        try (
                MockedStatic<SecurityUtils> mockedSecurity = mockStatic(SecurityUtils.class);
                MockedStatic<ServletUriComponentsBuilder> mockedServlet = mockStatic(ServletUriComponentsBuilder.class)
        ) {
            mockedSecurity.when(SecurityUtils::getCurrentUsername).thenReturn("1");

            ServletUriComponentsBuilder builder = mock(ServletUriComponentsBuilder.class);
            when(builder.build()).thenReturn(mock(org.springframework.web.util.UriComponents.class));
            when(builder.build().toUriString()).thenReturn("http://localhost");
            mockedServlet.when(ServletUriComponentsBuilder::fromCurrentContextPath).thenReturn(builder);

            Account user = new Account();
            user.setAccountId(1L);
            user.setEmail("user@example.com");
            user.setFullName("Người dùng");

            Account admin = new Account();
            admin.setAccountId(2L);

            when(accountRepository.findById(1L)).thenReturn(Optional.of(user));
            when(accountRepository.findByRole_RoleName("ADMIN")).thenReturn(List.of(admin));

            MovieResponse response = movieService.createMovie(request);
            assertEquals("Tên VN", response.getNameVN());
        }
    }


    @Test
    void createMovie_shouldThrowDuplicateVN() {
        when(movieRepository.findByNameVNAndIsDeletedFalse("Tên VN"))
                .thenReturn(Optional.of(new Movie()));

        assertThrows(DuplicateNameException.class, () -> movieService.createMovie(request));
    }

    @Test
    void updateMovie_shouldSucceed() throws Exception {
        Movie existingMovie = new Movie();
        existingMovie.setId(1L);
        existingMovie.setNameVN("Old VN");
        existingMovie.setNameEN("Old EN");

        when(movieRepository.findById(1L)).thenReturn(Optional.of(existingMovie));
        when(movieRepository.findByNameVNAndIsDeletedFalse("Tên VN")).thenReturn(Optional.empty());
        when(movieRepository.findByNameENAndIsDeletedFalse("Tên EN")).thenReturn(Optional.empty());
        when(movieRepository.save(any(Movie.class))).thenAnswer(i -> i.getArgument(0));
        when(typeRepository.findAllByIdIn(List.of(1L))).thenReturn(List.of(new Type(1L, "Hành động", false)));

        when(smallImage.isEmpty()).thenReturn(false);
        when(largeImage.isEmpty()).thenReturn(false);
        when(smallImage.getOriginalFilename()).thenReturn("small.jpg");
        when(largeImage.getOriginalFilename()).thenReturn("large.jpg");
        when(smallImage.getInputStream()).thenReturn(new ByteArrayInputStream("dummy".getBytes()));
        when(largeImage.getInputStream()).thenReturn(new ByteArrayInputStream("dummy".getBytes()));

        try (
                MockedStatic<SecurityUtils> mockedSecurity = mockStatic(SecurityUtils.class);
                MockedStatic<ServletUriComponentsBuilder> mockedServlet = mockStatic(ServletUriComponentsBuilder.class)
        ) {
            mockedSecurity.when(SecurityUtils::getCurrentUsername).thenReturn("1");

            ServletUriComponentsBuilder builder = mock(ServletUriComponentsBuilder.class);
            when(builder.build()).thenReturn(mock(org.springframework.web.util.UriComponents.class));
            when(builder.build().toUriString()).thenReturn("http://localhost");
            mockedServlet.when(ServletUriComponentsBuilder::fromCurrentContextPath).thenReturn(builder);

            Account user = new Account();
            user.setAccountId(1L);
            user.setEmail("user@example.com");
            user.setFullName("Người dùng");

            Account admin = new Account();
            admin.setAccountId(2L);

            when(accountRepository.findById(1L)).thenReturn(Optional.of(user));
            when(accountRepository.findByRole_RoleName("ADMIN")).thenReturn(List.of(admin));

            MovieResponse result = movieService.updateMovie(1L, request);
            assertEquals("Tên VN", result.getNameVN());
            assertEquals("Tên EN", result.getNameEN());
        }
    }


    @Test
    void updateMovie_shouldThrowNotFound() {
        when(movieRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> movieService.updateMovie(1L, request));
    }

    @Test
    void getAllMovies_shouldReturnList() {
        when(movieRepository.findByIsDeletedFalse()).thenReturn(List.of(movie));
        when(movieTypeRepository.findAll()).thenReturn(List.of(
                new MovieType(Integer.valueOf(1), movie, new Type(1L, "Hành động", false))
        ));

        List<MovieResponse> list = movieService.getAllMovies();
        assertEquals(1, list.size());
        assertEquals("Tên VN", list.get(0).getNameVN());
    }

    @Test
    void getMovieById_shouldThrowIfNotFound() {
        when(movieRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> movieService.getMovieById(1L));
    }

    @Test
    void deleteMovie_shouldSucceed() {
        movie.setIsDeleted(false);
        when(movieRepository.findById(1L)).thenReturn(Optional.of(movie));
        when(movieRepository.save(movie)).thenReturn(movie);

        try (MockedStatic<SecurityUtils> mocked = mockStatic(SecurityUtils.class)) {
            mocked.when(SecurityUtils::getCurrentUsername).thenReturn("1");

            Account user = new Account();
            user.setAccountId(1L);
            user.setEmail("user@example.com");
            user.setFullName("Người dùng");

            Account admin = new Account();
            admin.setAccountId(2L);

            when(accountRepository.findById(1L)).thenReturn(Optional.of(user));
            when(accountRepository.findByRole_RoleName("ADMIN")).thenReturn(List.of(admin));

            movieService.deleteMovie(1L);
            assertTrue(movie.getIsDeleted());
        }
    }
}
