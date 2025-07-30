package com.example.demo.service;

import com.example.demo.DTO.response.dashboard.BookingTicketRecentlyResponse;
import com.example.demo.DTO.response.dashboard.DailyTicketRevenueResponse;
import com.example.demo.exception.AppException;
import com.example.demo.repository.BookingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for BookingService (focused on revenue and statistics methods)
 * Testing revenue calculation, daily/weekly/monthly statistics, and recent
 * bookings
 */
@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    @InjectMocks
    private BookingService bookingService;

    private List<DailyTicketRevenueResponse> mockDailyStats;
    private List<DailyTicketRevenueResponse> mockWeeklyStats;
    private List<DailyTicketRevenueResponse> mockMonthlyStats;
    private List<BookingTicketRecentlyResponse> mockRecentBookings;

    @BeforeEach
    void setUp() {
        // Setup mock data for daily statistics
        mockDailyStats = new ArrayList<>();
        LocalDateTime today = LocalDateTime.now().toLocalDate().atStartOfDay();
        mockDailyStats.add(new DailyTicketRevenueResponse(
                Timestamp.valueOf(today.minusDays(2)),
                new BigDecimal("500000"),
                10L));
        mockDailyStats.add(new DailyTicketRevenueResponse(
                Timestamp.valueOf(today.minusDays(1)),
                new BigDecimal("750000"),
                15L));

        // Setup mock data for weekly statistics
        mockWeeklyStats = new ArrayList<>();
        LocalDateTime thisWeek = LocalDateTime.now().with(DayOfWeek.MONDAY).toLocalDate().atStartOfDay();
        mockWeeklyStats.add(new DailyTicketRevenueResponse(
                Timestamp.valueOf(thisWeek.minusWeeks(1)),
                new BigDecimal("2000000"),
                40L));

        // Setup mock data for monthly statistics
        mockMonthlyStats = new ArrayList<>();
        LocalDateTime thisMonth = LocalDateTime.now().withDayOfMonth(1).toLocalDate().atStartOfDay();
        mockMonthlyStats.add(new DailyTicketRevenueResponse(
                Timestamp.valueOf(thisMonth.minusMonths(1)),
                new BigDecimal("8000000"),
                160L));

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
        mockRecentBookings.add(BookingTicketRecentlyResponse.builder()
                .bookingId(2)
                .fullName("Jane Smith")
                .email("jane@example.com")
                .movieTitle("Spider-Man")
                .cinemaRoomName("Room B")
                .bookingDate(LocalDateTime.now().minusHours(5))
                .seatCount(1L)
                .totalPrice(new BigDecimal("100000"))
                .paymentMethod("VNPAY")
                .paymentStatus("PAID")
                .build());
    }

    // ========== GET TOTAL REVENUE BY STATUS TESTS ==========

    @Test
    void testGetTotalRevenueByStatus_whenValidStatusWithRevenue_shouldReturnTotalRevenue() {
        // Arrange
        String status = "PAID";
        Long expectedRevenue = 5000000L;
        when(bookingRepository.getTotalRevenueByStatus(status))
                .thenReturn(Optional.of(expectedRevenue));

        // Act
        Long result = bookingService.getTotalRevenueByStatus(status);

        // Assert
        assertNotNull(result);
        assertEquals(expectedRevenue, result);
        verify(bookingRepository).getTotalRevenueByStatus(status);
    }

    @Test
    void testGetTotalRevenueByStatus_whenValidStatusWithNoRevenue_shouldThrowAppException() {
        // Arrange
        String status = "CANCELLED";
        when(bookingRepository.getTotalRevenueByStatus(status))
                .thenReturn(Optional.empty());

        // Act & Assert
        AppException exception = assertThrows(AppException.class,
                () -> bookingService.getTotalRevenueByStatus(status));
        assertEquals("No revenue data found for status: " + status, exception.getMessage());
        verify(bookingRepository).getTotalRevenueByStatus(status);
    }

    @Test
    void testGetTotalRevenueByStatus_whenNullStatus_shouldCallRepositoryWithNull() {
        // Arrange
        when(bookingRepository.getTotalRevenueByStatus(null))
                .thenReturn(Optional.empty());

        // Act & Assert
        AppException exception = assertThrows(AppException.class,
                () -> bookingService.getTotalRevenueByStatus(null));
        assertEquals("No revenue data found for status: null", exception.getMessage());
        verify(bookingRepository).getTotalRevenueByStatus(null);
    }

    @Test
    void testGetTotalRevenueByStatus_whenEmptyStatus_shouldCallRepositoryWithEmptyString() {
        // Arrange
        String status = "";
        when(bookingRepository.getTotalRevenueByStatus(status))
                .thenReturn(Optional.empty());

        // Act & Assert
        AppException exception = assertThrows(AppException.class,
                () -> bookingService.getTotalRevenueByStatus(status));
        assertEquals("No revenue data found for status: ", exception.getMessage());
        verify(bookingRepository).getTotalRevenueByStatus(status);
    }

    // ========== GET DAILY TICKET REVENUE TESTS ==========

    @Test
    void testGetDailyTicketRevenue_whenValidDayCount_shouldReturnCompleteList() {
        // Arrange
        int dayCount = 7;
        when(bookingRepository.getDailyTicketRevenue(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(mockDailyStats);

        // Act
        List<DailyTicketRevenueResponse> result = bookingService.getDailyTicketRevenue(dayCount);

        // Assert
        assertNotNull(result);
        assertEquals(dayCount, result.size());
        verify(bookingRepository).getDailyTicketRevenue(any(LocalDateTime.class), any(LocalDateTime.class));

        // Verify that missing days are filled with zero values
        long zeroRevenueCount = result.stream()
                .filter(r -> r.getRevenue().equals(BigDecimal.ZERO))
                .count();
        assertTrue(zeroRevenueCount > 0, "Should have days with zero revenue for missing data");
    }

    @Test
    void testGetDailyTicketRevenue_whenZeroDayCount_shouldReturnEmptyList() {
        // Arrange
        int dayCount = 0;
        when(bookingRepository.getDailyTicketRevenue(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(new ArrayList<>());

        // Act
        List<DailyTicketRevenueResponse> result = bookingService.getDailyTicketRevenue(dayCount);

        // Assert
        assertNotNull(result);
        assertEquals(0, result.size());
        verify(bookingRepository).getDailyTicketRevenue(any(LocalDateTime.class), any(LocalDateTime.class));
    }

    @Test
    void testGetDailyTicketRevenue_whenRepositoryReturnsEmpty_shouldFillWithZeroValues() {
        // Arrange
        int dayCount = 3;
        when(bookingRepository.getDailyTicketRevenue(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(new ArrayList<>());

        // Act
        List<DailyTicketRevenueResponse> result = bookingService.getDailyTicketRevenue(dayCount);

        // Assert
        assertNotNull(result);
        assertEquals(dayCount, result.size());

        // All days should have zero revenue and tickets
        for (DailyTicketRevenueResponse response : result) {
            assertEquals(BigDecimal.ZERO, response.getRevenue());
            assertEquals(0L, response.getTickets());
        }
        verify(bookingRepository).getDailyTicketRevenue(any(LocalDateTime.class), any(LocalDateTime.class));
    }

    @Test
    void testGetDailyTicketRevenue_whenNegativeDayCount_shouldHandleGracefully() {
        // Arrange
        int dayCount = -1;
        when(bookingRepository.getDailyTicketRevenue(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(new ArrayList<>());

        // Act
        List<DailyTicketRevenueResponse> result = bookingService.getDailyTicketRevenue(dayCount);

        // Assert
        assertNotNull(result);
        assertEquals(0, result.size()); // Negative count should result in empty list
        verify(bookingRepository).getDailyTicketRevenue(any(LocalDateTime.class), any(LocalDateTime.class));
    }

    // ========== GET WEEKLY TICKET REVENUE TESTS ==========

    @Test
    void testGetWeeklyTicketRevenue_whenValidWeekCount_shouldReturnCompleteList() {
        // Arrange
        int weekCount = 6;
        when(bookingRepository.getWeeklyRevenueAndTickets(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(mockWeeklyStats);

        // Act
        List<DailyTicketRevenueResponse> result = bookingService.getWeeklyTicketRevenue(weekCount);

        // Assert
        assertNotNull(result);
        assertEquals(weekCount, result.size());
        verify(bookingRepository).getWeeklyRevenueAndTickets(any(LocalDateTime.class), any(LocalDateTime.class));

        // Verify that missing weeks are filled with zero values
        long zeroRevenueCount = result.stream()
                .filter(r -> r.getRevenue().equals(BigDecimal.ZERO))
                .count();
        assertTrue(zeroRevenueCount > 0, "Should have weeks with zero revenue for missing data");
    }

    @Test
    void testGetWeeklyTicketRevenue_whenZeroWeekCount_shouldReturnEmptyList() {
        // Arrange
        int weekCount = 0;
        when(bookingRepository.getWeeklyRevenueAndTickets(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(new ArrayList<>());

        // Act
        List<DailyTicketRevenueResponse> result = bookingService.getWeeklyTicketRevenue(weekCount);

        // Assert
        assertNotNull(result);
        assertEquals(0, result.size());
        verify(bookingRepository).getWeeklyRevenueAndTickets(any(LocalDateTime.class), any(LocalDateTime.class));
    }

    @Test
    void testGetWeeklyTicketRevenue_whenRepositoryReturnsEmpty_shouldFillWithZeroValues() {
        // Arrange
        int weekCount = 4;
        when(bookingRepository.getWeeklyRevenueAndTickets(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(new ArrayList<>());

        // Act
        List<DailyTicketRevenueResponse> result = bookingService.getWeeklyTicketRevenue(weekCount);

        // Assert
        assertNotNull(result);
        assertEquals(weekCount, result.size());

        // All weeks should have zero revenue and tickets
        for (DailyTicketRevenueResponse response : result) {
            assertEquals(BigDecimal.ZERO, response.getRevenue());
            assertEquals(0L, response.getTickets());
        }
        verify(bookingRepository).getWeeklyRevenueAndTickets(any(LocalDateTime.class), any(LocalDateTime.class));
    }

    @Test
    void testGetWeeklyTicketRevenue_whenMondayAlignment_shouldStartFromMonday() {
        // Arrange
        int weekCount = 2;
        when(bookingRepository.getWeeklyRevenueAndTickets(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(new ArrayList<>());

        // Act
        List<DailyTicketRevenueResponse> result = bookingService.getWeeklyTicketRevenue(weekCount);

        // Assert
        assertNotNull(result);
        assertEquals(weekCount, result.size());

        // Verify repository is called with Monday-aligned dates
        verify(bookingRepository).getWeeklyRevenueAndTickets(
                argThat(fromDate -> fromDate.getDayOfWeek() == DayOfWeek.MONDAY),
                argThat(toDate -> toDate.getDayOfWeek() == DayOfWeek.MONDAY));
    }

    // ========== GET MONTHLY TICKET REVENUE TESTS ==========

    @Test
    void testGetMonthlyTicketRevenue_whenValidMonthCount_shouldReturnCompleteList() {
        // Arrange
        int monthCount = 6;
        when(bookingRepository.getMonthlyRevenueAndTickets(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(mockMonthlyStats);

        // Act
        List<DailyTicketRevenueResponse> result = bookingService.getMonthlyTicketRevenue(monthCount);

        // Assert
        assertNotNull(result);
        assertEquals(monthCount, result.size());
        verify(bookingRepository).getMonthlyRevenueAndTickets(any(LocalDateTime.class), any(LocalDateTime.class));

        // Verify that missing months are filled with zero values
        long zeroRevenueCount = result.stream()
                .filter(r -> r.getRevenue().equals(BigDecimal.ZERO))
                .count();
        assertTrue(zeroRevenueCount > 0, "Should have months with zero revenue for missing data");
    }

    @Test
    void testGetMonthlyTicketRevenue_whenZeroMonthCount_shouldReturnEmptyList() {
        // Arrange
        int monthCount = 0;
        when(bookingRepository.getMonthlyRevenueAndTickets(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(new ArrayList<>());

        // Act
        List<DailyTicketRevenueResponse> result = bookingService.getMonthlyTicketRevenue(monthCount);

        // Assert
        assertNotNull(result);
        assertEquals(0, result.size());
        verify(bookingRepository).getMonthlyRevenueAndTickets(any(LocalDateTime.class), any(LocalDateTime.class));
    }

    @Test
    void testGetMonthlyTicketRevenue_whenRepositoryReturnsEmpty_shouldFillWithZeroValues() {
        // Arrange
        int monthCount = 3;
        when(bookingRepository.getMonthlyRevenueAndTickets(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(new ArrayList<>());

        // Act
        List<DailyTicketRevenueResponse> result = bookingService.getMonthlyTicketRevenue(monthCount);

        // Assert
        assertNotNull(result);
        assertEquals(monthCount, result.size());

        // All months should have zero revenue and tickets
        for (DailyTicketRevenueResponse response : result) {
            assertEquals(BigDecimal.ZERO, response.getRevenue());
            assertEquals(0L, response.getTickets());
        }
        verify(bookingRepository).getMonthlyRevenueAndTickets(any(LocalDateTime.class), any(LocalDateTime.class));
    }

    @Test
    void testGetMonthlyTicketRevenue_whenFirstDayAlignment_shouldStartFromFirstDay() {
        // Arrange
        int monthCount = 2;
        when(bookingRepository.getMonthlyRevenueAndTickets(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(new ArrayList<>());

        // Act
        List<DailyTicketRevenueResponse> result = bookingService.getMonthlyTicketRevenue(monthCount);

        // Assert
        assertNotNull(result);
        assertEquals(monthCount, result.size());

        // Verify repository is called with first-day-of-month aligned dates
        verify(bookingRepository).getMonthlyRevenueAndTickets(
                argThat(fromDate -> fromDate.getDayOfMonth() == 1),
                argThat(toDate -> toDate.getDayOfMonth() == 1));
    }

    // ========== GET RECENTLY BOOKED TICKETS TESTS ==========

    @Test
    void testGetRecentlyBookedTickets_whenValidLimit_shouldReturnBookingList() {
        // Arrange
        int limit = 30;
        when(bookingRepository.getBookingTicketRecently(limit))
                .thenReturn(mockRecentBookings);

        // Act
        List<BookingTicketRecentlyResponse> result = bookingService.getRecentlyBookedTickets(limit);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("John Doe", result.get(0).getFullName());
        assertEquals("Avengers", result.get(0).getMovieTitle());
        assertEquals("Jane Smith", result.get(1).getFullName());
        assertEquals("Spider-Man", result.get(1).getMovieTitle());
        verify(bookingRepository).getBookingTicketRecently(limit);
    }

    @Test
    void testGetRecentlyBookedTickets_whenZeroLimit_shouldReturnEmptyList() {
        // Arrange
        int limit = 0;
        when(bookingRepository.getBookingTicketRecently(limit))
                .thenReturn(new ArrayList<>());

        // Act
        List<BookingTicketRecentlyResponse> result = bookingService.getRecentlyBookedTickets(limit);

        // Assert
        assertNotNull(result);
        assertEquals(0, result.size());
        verify(bookingRepository).getBookingTicketRecently(limit);
    }

    @Test
    void testGetRecentlyBookedTickets_whenNegativeLimit_shouldCallRepository() {
        // Arrange
        int limit = -5;
        when(bookingRepository.getBookingTicketRecently(limit))
                .thenReturn(new ArrayList<>());

        // Act
        List<BookingTicketRecentlyResponse> result = bookingService.getRecentlyBookedTickets(limit);

        // Assert
        assertNotNull(result);
        assertEquals(0, result.size());
        verify(bookingRepository).getBookingTicketRecently(limit);
    }

    @Test
    void testGetRecentlyBookedTickets_whenRepositoryReturnsNull_shouldHandleGracefully() {
        // Arrange
        int limit = 10;
        when(bookingRepository.getBookingTicketRecently(limit))
                .thenReturn(null);

        // Act
        List<BookingTicketRecentlyResponse> result = bookingService.getRecentlyBookedTickets(limit);

        // Assert
        assertNull(result);
        verify(bookingRepository).getBookingTicketRecently(limit);
    }

    @Test
    void testGetRecentlyBookedTickets_whenLargeLimit_shouldReturnAllAvailableBookings() {
        // Arrange
        int limit = 1000;
        when(bookingRepository.getBookingTicketRecently(limit))
                .thenReturn(mockRecentBookings);

        // Act
        List<BookingTicketRecentlyResponse> result = bookingService.getRecentlyBookedTickets(limit);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size()); // Only 2 bookings available in mock data
        verify(bookingRepository).getBookingTicketRecently(limit);
    }

    @Test
    void testGetRecentlyBookedTickets_shouldReturnBookingsWithCorrectStructure() {
        // Arrange
        int limit = 5;
        when(bookingRepository.getBookingTicketRecently(limit))
                .thenReturn(mockRecentBookings);

        // Act
        List<BookingTicketRecentlyResponse> result = bookingService.getRecentlyBookedTickets(limit);

        // Assert
        assertNotNull(result);
        assertFalse(result.isEmpty());

        BookingTicketRecentlyResponse firstBooking = result.get(0);
        assertNotNull(firstBooking.getBookingId());
        assertNotNull(firstBooking.getFullName());
        assertNotNull(firstBooking.getEmail());
        assertNotNull(firstBooking.getMovieTitle());
        assertNotNull(firstBooking.getCinemaRoomName());
        assertNotNull(firstBooking.getBookingDate());
        assertNotNull(firstBooking.getSeatCount());
        assertNotNull(firstBooking.getTotalPrice());
        assertNotNull(firstBooking.getPaymentMethod());
        assertNotNull(firstBooking.getPaymentStatus());

        verify(bookingRepository).getBookingTicketRecently(limit);
    }
}
