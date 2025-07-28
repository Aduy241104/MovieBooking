package com.example.demo.service;

import com.example.demo.DTO.response.dashboard.*;
import com.example.demo.exception.AppException;
import com.example.demo.repository.MovieRepository;
import com.example.demo.repository.MovieTypeRepository;
import com.example.demo.repository.RoleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for DashboardService
 * Testing dashboard statistics aggregation and summary data generation
 */
@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @Mock
    private MovieScheduleService movieScheduleService;

    @Mock
    private BookingService bookingService;

    @Mock
    private ReviewService reviewService;

    @Mock
    private PromotionService promotionService;

    @Mock
    private BookedSeatService bookedSeatService;

    @Mock
    private AccountService accountService;

    @Mock
    private CinemaRoomService cinemaRoomService;

    @Mock
    private MovieTypeRepository movieTypeRepository;

    @Mock
    private MovieRepository movieRepository;

    @Mock
    private RoleRepository roleRepository;

    @InjectMocks
    private DashboardService dashboardService;

    private List<MovieTypeRevenueResponse.MovieTypeRevenue> mockMovieTypeRevenue;
    private List<DailyTicketRevenueResponse> mockDailyRevenue;
    private List<DailyTicketRevenueResponse> mockWeeklyRevenue;
    private List<DailyTicketRevenueResponse> mockMonthlyRevenue;
    private List<UserRegistrationsResponse> mockUserRegistrations;
    private List<TopMovieByRevenueResponse> mockTopMovies;
    private List<BookingTicketRecentlyResponse> mockRecentBookings;

    @BeforeEach
    void setUp() {
        // Setup mock data for movie type revenue
        mockMovieTypeRevenue = new ArrayList<>();
        mockMovieTypeRevenue.add(MovieTypeRevenueResponse.MovieTypeRevenue.builder()
                .movieType("Action")
                .quantity(15L)
                .revenue(new BigDecimal("5000000"))
                .build());
        mockMovieTypeRevenue.add(MovieTypeRevenueResponse.MovieTypeRevenue.builder()
                .movieType("Comedy")
                .quantity(10L)
                .revenue(new BigDecimal("3000000"))
                .build());

        // Setup mock data for daily revenue
        mockDailyRevenue = new ArrayList<>();
        mockDailyRevenue.add(new DailyTicketRevenueResponse(
                Timestamp.valueOf(LocalDateTime.now().minusDays(1)),
                new BigDecimal("500000"),
                10L));

        // Setup mock data for weekly revenue
        mockWeeklyRevenue = new ArrayList<>();
        mockWeeklyRevenue.add(new DailyTicketRevenueResponse(
                Timestamp.valueOf(LocalDateTime.now().minusWeeks(1)),
                new BigDecimal("2000000"),
                40L));

        // Setup mock data for monthly revenue
        mockMonthlyRevenue = new ArrayList<>();
        mockMonthlyRevenue.add(new DailyTicketRevenueResponse(
                Timestamp.valueOf(LocalDateTime.now().minusMonths(1)),
                new BigDecimal("8000000"),
                160L));

        // Setup mock data for user registrations
        mockUserRegistrations = new ArrayList<>();
        mockUserRegistrations.add(UserRegistrationsResponse.builder()
                .date(LocalDate.now().minusMonths(1))
                .newUsers(50L)
                .totalUsers(500L)
                .build());
        mockUserRegistrations.add(UserRegistrationsResponse.builder()
                .date(LocalDate.now().minusMonths(2))
                .newUsers(30L)
                .totalUsers(450L)
                .build());

        // Setup mock data for top movies
        mockTopMovies = new ArrayList<>();
        mockTopMovies.add(TopMovieByRevenueResponse.builder()
                .movieTitle("Avengers: Endgame")
                .movieGenre("Action")
                .avgRating(new BigDecimal("4.5"))
                .ticketSold(1000L)
                .revenue(new BigDecimal("2000000"))
                .posterUrl("http://example.com/avengers.jpg")
                .build());
        mockTopMovies.add(TopMovieByRevenueResponse.builder()
                .movieTitle("Spider-Man")
                .movieGenre("Action")
                .avgRating(new BigDecimal("4.2"))
                .ticketSold(800L)
                .revenue(new BigDecimal("1600000"))
                .posterUrl("http://example.com/spiderman.jpg")
                .build());

        // Setup mock data for recent bookings
        mockRecentBookings = new ArrayList<>();
        mockRecentBookings.add(BookingTicketRecentlyResponse.builder()
                .bookingId(1)
                .fullName("John Doe")
                .email("john@example.com")
                .movieTitle("Avengers")
                .cinemaRoomName("Room A")
                .bookingDate(LocalDateTime.now().minusHours(2))
                .seatCount(2L)
                .totalPrice(new BigDecimal("200000"))
                .paymentMethod("VNPAY")
                .paymentStatus("PAID")
                .build());
    }

    // ========== GET MOVIE TYPE REVENUE TESTS ==========

    @Test
    void testGetMovieTypeRevenue_whenDataExists_shouldReturnMovieTypeRevenueResponse() {
        // Arrange
        when(movieTypeRepository.getMoviesByTypeRevenue())
                .thenReturn(mockMovieTypeRevenue);

        // Act
        MovieTypeRevenueResponse result = dashboardService.getMovieTypeRevenue();

        // Assert
        assertNotNull(result);
        assertNotNull(result.getData());
        assertEquals(2, result.getData().size());

        MovieTypeRevenueResponse.MovieTypeRevenue firstType = result.getData().get(0);
        assertEquals("Action", firstType.getMovieType());
        assertEquals(15L, firstType.getQuantity());
        assertEquals(new BigDecimal("5000000"), firstType.getRevenue());

        MovieTypeRevenueResponse.MovieTypeRevenue secondType = result.getData().get(1);
        assertEquals("Comedy", secondType.getMovieType());
        assertEquals(10L, secondType.getQuantity());
        assertEquals(new BigDecimal("3000000"), secondType.getRevenue());

        verify(movieTypeRepository).getMoviesByTypeRevenue();
    }

    @Test
    void testGetMovieTypeRevenue_whenNoDataExists_shouldReturnEmptyResponse() {
        // Arrange
        when(movieTypeRepository.getMoviesByTypeRevenue())
                .thenReturn(new ArrayList<>());

        // Act
        MovieTypeRevenueResponse result = dashboardService.getMovieTypeRevenue();

        // Assert
        assertNotNull(result);
        assertNotNull(result.getData());
        assertEquals(0, result.getData().size());
        verify(movieTypeRepository).getMoviesByTypeRevenue();
    }

    @Test
    void testGetMovieTypeRevenue_whenRepositoryReturnsNull_shouldReturnResponseWithNullData() {
        // Arrange
        when(movieTypeRepository.getMoviesByTypeRevenue())
                .thenReturn(null);

        // Act
        MovieTypeRevenueResponse result = dashboardService.getMovieTypeRevenue();

        // Assert
        assertNotNull(result);
        assertNull(result.getData());
        verify(movieTypeRepository).getMoviesByTypeRevenue();
    }

    // ========== GET DASHBOARD SUMMARY TESTS ==========

    @Test
    void testGetDashboardSummary_whenAllDataExists_shouldReturnCompleteSummary() {
        // Arrange - Mock all service calls
        when(bookingService.getTotalRevenueByStatus("PAID")).thenReturn(10000000L);
        when(bookedSeatService.totalBookedSeats()).thenReturn(5000L);
        when(roleRepository.existsByRoleName("CUSTOMER")).thenReturn(true);
        when(accountService.getTotalAccountByRole("CUSTOMER")).thenReturn(1000L);
        when(movieScheduleService.getTotalNowShowingMovie()).thenReturn(50L);
        when(reviewService.getTotalApprovedReviews()).thenReturn(2000L);
        when(reviewService.findAverageRatingOfApproved()).thenReturn(4.2);
        when(promotionService.getTotalActivePromotions()).thenReturn(15L);
        when(cinemaRoomService.getTotalCinemaRoom()).thenReturn(10L);

        when(bookingService.getDailyTicketRevenue(7)).thenReturn(mockDailyRevenue);
        when(bookingService.getWeeklyTicketRevenue(6)).thenReturn(mockWeeklyRevenue);
        when(bookingService.getMonthlyTicketRevenue(6)).thenReturn(mockMonthlyRevenue);
        when(movieTypeRepository.getMoviesByTypeRevenue()).thenReturn(mockMovieTypeRevenue);
        when(accountService.getUserRegistrationsDTO(3)).thenReturn(mockUserRegistrations);
        when(movieRepository.getTopMoviesByRevenue()).thenReturn(mockTopMovies);
        when(bookingService.getRecentlyBookedTickets(30)).thenReturn(mockRecentBookings);

        // Act
        DashboardSummaryResponse result = dashboardService.getDashboardSummary();

        // Assert
        assertNotNull(result);
        assertNotNull(result.getData());

        DashboardSummaryResponse.DataSummary data = result.getData();
        assertEquals(10000000L, data.getTotalRevenue());
        assertEquals(5000L, data.getTotalBookings());
        assertEquals(1000L, data.getTotalUsers());
        assertEquals(50L, data.getActiveMovies());
        assertEquals(2000L, data.getTotalReviews());
        assertEquals(4.2, data.getAvgRating());
        assertEquals(15L, data.getActivePromotions());
        assertEquals(10L, data.getTotalCinemaRooms());

        // Verify charts data
        assertNotNull(data.getCharts());
        assertNotNull(data.getCharts().getRevenue());
        assertEquals(mockDailyRevenue, data.getCharts().getRevenue().getDay());
        assertEquals(mockWeeklyRevenue, data.getCharts().getRevenue().getWeek());
        assertEquals(mockMonthlyRevenue, data.getCharts().getRevenue().getMonth());
        assertEquals(mockMovieTypeRevenue, data.getCharts().getMoviesByType());
        assertEquals(2, data.getCharts().getUserRegistrations().size());

        // Verify tables data
        assertNotNull(data.getTables());
        assertEquals(mockTopMovies, data.getTables().getTopMovies());
        assertEquals(mockRecentBookings, data.getTables().getRecentBookings());

        // Verify all service calls
        verify(bookingService).getTotalRevenueByStatus("PAID");
        verify(bookedSeatService).totalBookedSeats();
        verify(roleRepository).existsByRoleName("CUSTOMER");
        verify(accountService).getTotalAccountByRole("CUSTOMER");
        verify(movieScheduleService).getTotalNowShowingMovie();
        verify(reviewService).getTotalApprovedReviews();
        verify(reviewService).findAverageRatingOfApproved();
        verify(promotionService).getTotalActivePromotions();
        verify(cinemaRoomService).getTotalCinemaRoom();
        verify(bookingService).getDailyTicketRevenue(7);
        verify(bookingService).getWeeklyTicketRevenue(6);
        verify(bookingService).getMonthlyTicketRevenue(6);
        verify(movieTypeRepository).getMoviesByTypeRevenue();
        verify(accountService).getUserRegistrationsDTO(3);
        verify(movieRepository).getTopMoviesByRevenue();
        verify(bookingService).getRecentlyBookedTickets(30);
    }

    @Test
    void testGetDashboardSummary_whenCustomerRoleNotExists_shouldThrowAppException() {
        // Arrange
        when(bookingService.getTotalRevenueByStatus("PAID")).thenReturn(10000000L);
        when(bookedSeatService.totalBookedSeats()).thenReturn(5000L);
        when(roleRepository.existsByRoleName("CUSTOMER")).thenReturn(false);

        // Act & Assert
        AppException exception = assertThrows(AppException.class,
                () -> dashboardService.getDashboardSummary());
        assertEquals("Role not found: CUSTOMER", exception.getMessage());

        verify(bookingService).getTotalRevenueByStatus("PAID");
        verify(bookedSeatService).totalBookedSeats();
        verify(roleRepository).existsByRoleName("CUSTOMER");
        verify(accountService, never()).getTotalAccountByRole(anyString());
    }

    @Test
    void testGetDashboardSummary_whenSomeServicesReturnNull_shouldHandleGracefully() {
        // Arrange - Some services return null values
        when(bookingService.getTotalRevenueByStatus("PAID")).thenReturn(null);
        when(bookedSeatService.totalBookedSeats()).thenReturn(null);
        when(roleRepository.existsByRoleName("CUSTOMER")).thenReturn(true);
        when(accountService.getTotalAccountByRole("CUSTOMER")).thenReturn(0L);
        when(movieScheduleService.getTotalNowShowingMovie()).thenReturn(0L);
        when(reviewService.getTotalApprovedReviews()).thenReturn(0L);
        when(reviewService.findAverageRatingOfApproved()).thenReturn(null);
        when(promotionService.getTotalActivePromotions()).thenReturn(null);
        when(cinemaRoomService.getTotalCinemaRoom()).thenReturn(null);

        when(bookingService.getDailyTicketRevenue(7)).thenReturn(new ArrayList<>());
        when(bookingService.getWeeklyTicketRevenue(6)).thenReturn(new ArrayList<>());
        when(bookingService.getMonthlyTicketRevenue(6)).thenReturn(new ArrayList<>());
        when(movieTypeRepository.getMoviesByTypeRevenue()).thenReturn(new ArrayList<>());
        when(accountService.getUserRegistrationsDTO(3)).thenReturn(new ArrayList<>());
        when(movieRepository.getTopMoviesByRevenue()).thenReturn(new ArrayList<>());
        when(bookingService.getRecentlyBookedTickets(30)).thenReturn(new ArrayList<>());

        // Act
        DashboardSummaryResponse result = dashboardService.getDashboardSummary();

        // Assert
        assertNotNull(result);
        assertNotNull(result.getData());

        DashboardSummaryResponse.DataSummary data = result.getData();
        assertNull(data.getTotalRevenue());
        assertNull(data.getTotalBookings());
        assertEquals(0L, data.getTotalUsers()); // getTotalAccountByRole returns 0L, not null
        assertEquals(0L, data.getActiveMovies()); // getTotalNowShowingMovie returns 0L, not null
        assertEquals(0L, data.getTotalReviews()); // getTotalApprovedReviews returns 0L, not null
        assertNull(data.getAvgRating());
        assertNull(data.getActivePromotions());
        assertNull(data.getTotalCinemaRooms());

        // Charts and tables should still be initialized with empty data
        assertNotNull(data.getCharts());
        assertNotNull(data.getTables());
    }

    @Test
    void testGetDashboardSummary_whenUserRegistrationsExist_shouldFormatDatesCorrectly() {
        // Arrange - Setup minimal required mocks
        when(bookingService.getTotalRevenueByStatus("PAID")).thenReturn(1000000L);
        when(bookedSeatService.totalBookedSeats()).thenReturn(100L);
        when(roleRepository.existsByRoleName("CUSTOMER")).thenReturn(true);
        when(accountService.getTotalAccountByRole("CUSTOMER")).thenReturn(50L);
        when(movieScheduleService.getTotalNowShowingMovie()).thenReturn(5L);
        when(reviewService.getTotalApprovedReviews()).thenReturn(100L);
        when(reviewService.findAverageRatingOfApproved()).thenReturn(4.0);
        when(promotionService.getTotalActivePromotions()).thenReturn(5L);
        when(cinemaRoomService.getTotalCinemaRoom()).thenReturn(3L);

        when(bookingService.getDailyTicketRevenue(7)).thenReturn(new ArrayList<>());
        when(bookingService.getWeeklyTicketRevenue(6)).thenReturn(new ArrayList<>());
        when(bookingService.getMonthlyTicketRevenue(6)).thenReturn(new ArrayList<>());
        when(movieTypeRepository.getMoviesByTypeRevenue()).thenReturn(new ArrayList<>());
        when(accountService.getUserRegistrationsDTO(3)).thenReturn(mockUserRegistrations);
        when(movieRepository.getTopMoviesByRevenue()).thenReturn(new ArrayList<>());
        when(bookingService.getRecentlyBookedTickets(30)).thenReturn(new ArrayList<>());

        // Act
        DashboardSummaryResponse result = dashboardService.getDashboardSummary();

        // Assert
        assertNotNull(result);
        assertNotNull(result.getData().getCharts().getUserRegistrations());
        assertEquals(2, result.getData().getCharts().getUserRegistrations().size());

        // Verify date formatting (MM/yyyy format)
        DashboardSummaryResponse.UserRegistrationChartData firstRegistration = result.getData().getCharts()
                .getUserRegistrations().get(0);
        assertNotNull(firstRegistration.getDate());
        assertTrue(firstRegistration.getDate().matches("\\d{2}/\\d{4}")); // MM/yyyy format
        assertEquals(50L, firstRegistration.getNewUsers());
        assertEquals(500L, firstRegistration.getTotalUsers());
    }

    @Test
    void testGetDashboardSummary_shouldCallAllRequiredServicesOnce() {
        // Arrange - Setup all mocks
        when(bookingService.getTotalRevenueByStatus("PAID")).thenReturn(1000000L);
        when(bookedSeatService.totalBookedSeats()).thenReturn(100L);
        when(roleRepository.existsByRoleName("CUSTOMER")).thenReturn(true);
        when(accountService.getTotalAccountByRole("CUSTOMER")).thenReturn(50L);
        when(movieScheduleService.getTotalNowShowingMovie()).thenReturn(5L);
        when(reviewService.getTotalApprovedReviews()).thenReturn(100L);
        when(reviewService.findAverageRatingOfApproved()).thenReturn(4.0);
        when(promotionService.getTotalActivePromotions()).thenReturn(5L);
        when(cinemaRoomService.getTotalCinemaRoom()).thenReturn(3L);

        when(bookingService.getDailyTicketRevenue(7)).thenReturn(new ArrayList<>());
        when(bookingService.getWeeklyTicketRevenue(6)).thenReturn(new ArrayList<>());
        when(bookingService.getMonthlyTicketRevenue(6)).thenReturn(new ArrayList<>());
        when(movieTypeRepository.getMoviesByTypeRevenue()).thenReturn(new ArrayList<>());
        when(accountService.getUserRegistrationsDTO(3)).thenReturn(new ArrayList<>());
        when(movieRepository.getTopMoviesByRevenue()).thenReturn(new ArrayList<>());
        when(bookingService.getRecentlyBookedTickets(30)).thenReturn(new ArrayList<>());

        // Act
        dashboardService.getDashboardSummary();

        // Assert - Verify each service is called exactly once
        verify(bookingService, times(1)).getTotalRevenueByStatus("PAID");
        verify(bookedSeatService, times(1)).totalBookedSeats();
        verify(roleRepository, times(1)).existsByRoleName("CUSTOMER");
        verify(accountService, times(1)).getTotalAccountByRole("CUSTOMER");
        verify(movieScheduleService, times(1)).getTotalNowShowingMovie();
        verify(reviewService, times(1)).getTotalApprovedReviews();
        verify(reviewService, times(1)).findAverageRatingOfApproved();
        verify(promotionService, times(1)).getTotalActivePromotions();
        verify(cinemaRoomService, times(1)).getTotalCinemaRoom();
        verify(bookingService, times(1)).getDailyTicketRevenue(7);
        verify(bookingService, times(1)).getWeeklyTicketRevenue(6);
        verify(bookingService, times(1)).getMonthlyTicketRevenue(6);
        verify(movieTypeRepository, times(1)).getMoviesByTypeRevenue();
        verify(accountService, times(1)).getUserRegistrationsDTO(3);
        verify(movieRepository, times(1)).getTopMoviesByRevenue();
        verify(bookingService, times(1)).getRecentlyBookedTickets(30);

        // Verify no other interactions
        verifyNoMoreInteractions(bookingService, bookedSeatService, roleRepository,
                accountService, movieScheduleService, reviewService, promotionService,
                cinemaRoomService, movieTypeRepository, movieRepository);
    }

    @Test
    void testGetDashboardSummary_shouldUseCorrectParametersForDataRetrieval() {
        // Arrange
        when(bookingService.getTotalRevenueByStatus("PAID")).thenReturn(1000000L);
        when(bookedSeatService.totalBookedSeats()).thenReturn(100L);
        when(roleRepository.existsByRoleName("CUSTOMER")).thenReturn(true);
        when(accountService.getTotalAccountByRole("CUSTOMER")).thenReturn(50L);
        when(movieScheduleService.getTotalNowShowingMovie()).thenReturn(5L);
        when(reviewService.getTotalApprovedReviews()).thenReturn(100L);
        when(reviewService.findAverageRatingOfApproved()).thenReturn(4.0);
        when(promotionService.getTotalActivePromotions()).thenReturn(5L);
        when(cinemaRoomService.getTotalCinemaRoom()).thenReturn(3L);

        when(bookingService.getDailyTicketRevenue(anyInt())).thenReturn(new ArrayList<>());
        when(bookingService.getWeeklyTicketRevenue(anyInt())).thenReturn(new ArrayList<>());
        when(bookingService.getMonthlyTicketRevenue(anyInt())).thenReturn(new ArrayList<>());
        when(movieTypeRepository.getMoviesByTypeRevenue()).thenReturn(new ArrayList<>());
        when(accountService.getUserRegistrationsDTO(anyInt())).thenReturn(new ArrayList<>());
        when(movieRepository.getTopMoviesByRevenue()).thenReturn(new ArrayList<>());
        when(bookingService.getRecentlyBookedTickets(anyInt())).thenReturn(new ArrayList<>());

        // Act
        dashboardService.getDashboardSummary();

        // Assert - Verify correct parameters are used
        verify(bookingService).getTotalRevenueByStatus("PAID");
        verify(roleRepository).existsByRoleName("CUSTOMER");
        verify(accountService).getTotalAccountByRole("CUSTOMER");
        verify(bookingService).getDailyTicketRevenue(7);
        verify(bookingService).getWeeklyTicketRevenue(6);
        verify(bookingService).getMonthlyTicketRevenue(6);
        verify(accountService).getUserRegistrationsDTO(3);
        verify(bookingService).getRecentlyBookedTickets(30);
    }
}
