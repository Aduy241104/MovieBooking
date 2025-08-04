package com.example.demo.service;

import com.example.demo.model.*;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ExportExcelServiceTest {

    private ExportExcelService exportExcelService;
    private List<Booking> sampleBookings;


    @BeforeEach
    void setUp() {

        exportExcelService = new ExportExcelService();

        // --- Create mock data ---
        // Booking 1
        Account account1 = new Account();
        account1.setFullName("John Doe");
        account1.setEmail("johndoe@example.com");
        account1.setPhoneNumber("0901234567");

        Movie movie1 = new Movie();
        movie1.setNameVN("The Matrix");

        CinemaRoom room1 = new CinemaRoom();
        room1.setCinemaRoomName("Room 1");

        Screening screening1 = new Screening();
        screening1.setMovie(movie1);
        screening1.setCinemaRoom(room1);
        screening1.setShowDateTime(LocalDateTime.of(2025, 7, 30, 19, 30));

        PaymentMethod pm1 = new PaymentMethod();
        pm1.setName("VNPAY");

        Seat seatA1 = new Seat();
        seatA1.setSeatRow("A");
        seatA1.setSeatCol("1");

        Seat seatA2 = new Seat();
        seatA2.setSeatRow("A");
        seatA2.setSeatCol("2");

        Booking booking1 = new Booking();
        booking1.setBookingCode("CINE-TEST1");
        booking1.setScreening(screening1);
        booking1.setAccount(account1);
        booking1.setTotalAmount(new BigDecimal("220000"));

        // Using String for booking status as per project's design
        booking1.setBookingStatus("PAID");

        booking1.setPaymentMethod(pm1);
        booking1.setBookingTime(LocalDateTime.now());

        BookedSeat bs1 = new BookedSeat();
        bs1.setSeat(seatA1);
        BookedSeat bs2 = new BookedSeat();
        bs2.setSeat(seatA2);

        // Initialize the list before adding elements
        List<BookedSeat> bookedSeats = new ArrayList<>();
        bookedSeats.add(bs1);
        bookedSeats.add(bs2);
        booking1.setBookedSeats(bookedSeats);

        sampleBookings = new ArrayList<>();
        sampleBookings.add(booking1);
    }

    @Test
    void bookingsToExcel_WithValidData_ShouldCreateCorrectExcelFile() throws IOException {
        // 1. Act: Call the method to be tested.
        ByteArrayInputStream inputStream = exportExcelService.bookingsToExcel(sampleBookings, "TestSheet");

        // 2. Assert: Verify the results.
        assertNotNull(inputStream, "The generated InputStream should not be null.");

        // Read the Excel file back from the stream to inspect its content.
        Workbook workbook = new XSSFWorkbook(inputStream);
        Sheet sheet = workbook.getSheet("TestSheet");
        assertNotNull(sheet, "The sheet 'TestSheet' should exist.");

        // Check the header row.
        Row headerRow = sheet.getRow(0);
        assertEquals("Mã Vé", headerRow.getCell(0).getStringCellValue());
        assertEquals("Tên Phim", headerRow.getCell(1).getStringCellValue());
        // ... you can add more header checks if needed.

        // Check the number of data rows (1 header + 1 booking).
        assertEquals(1, sheet.getLastRowNum(), "There should be one header row and one data row.");

        // Check the content of the first data row (for booking1).
        Row dataRow = sheet.getRow(1);
        assertEquals("CINE-TEST1", dataRow.getCell(0).getStringCellValue());
        assertEquals("The Matrix", dataRow.getCell(1).getStringCellValue());
        // Check the concatenated seats column.
        assertEquals("A1, A2", dataRow.getCell(4).getStringCellValue());
        // Check the numeric total amount column.
        assertEquals(220000.0, dataRow.getCell(8).getNumericCellValue());
        // Check the status as a String.
        assertEquals("PAID", dataRow.getCell(9).getStringCellValue());

        workbook.close();
    }

    @Test
    void bookingsToExcel_WithEmptyList_ShouldCreateExcelWithOnlyHeader() throws IOException {
        // 1. Arrange: Create an empty list of bookings.
        List<Booking> emptyList = new ArrayList<>();

        // 2. Act
        ByteArrayInputStream inputStream = exportExcelService.bookingsToExcel(emptyList, "EmptySheet");

        // 3. Assert
        assertNotNull(inputStream);

        Workbook workbook = new XSSFWorkbook(inputStream);
        Sheet sheet = workbook.getSheet("EmptySheet");
        assertNotNull(sheet);

        // The sheet should only contain the header row.
        assertEquals(0, sheet.getLastRowNum(), "The sheet should only have one row (the header).");

        Row headerRow = sheet.getRow(0);
        assertNotNull(headerRow);
        assertEquals("Mã Vé", headerRow.getCell(0).getStringCellValue());

        workbook.close();
    }


    @Test
    void bookingsToExcel_WhenBookingDataIsNull_ShouldHandleGracefully() throws IOException {
        // Arrange
        Booking bookingWithNulls = new Booking();
        bookingWithNulls.setBookingCode("CINE-NULL");

        Screening screeningWithNulls = new Screening();
        screeningWithNulls.setMovie(new Movie());
        screeningWithNulls.setCinemaRoom(new CinemaRoom());
        screeningWithNulls.setShowDateTime(LocalDateTime.now());

        bookingWithNulls.setScreening(screeningWithNulls);
        bookingWithNulls.setAccount(new Account());
        bookingWithNulls.setPaymentMethod(new PaymentMethod());
        bookingWithNulls.setTotalAmount(BigDecimal.ZERO);
        bookingWithNulls.setBookingStatus("PAID");
        bookingWithNulls.setBookingTime(LocalDateTime.now());
        bookingWithNulls.setBookedSeats(new ArrayList<>());

        List<Booking> listWithNulls = List.of(bookingWithNulls);

        // Act
        ByteArrayInputStream inputStream = exportExcelService.bookingsToExcel(listWithNulls, "NullTest");

        // Assert & Debug
        assertNotNull(inputStream);
        Workbook workbook = new XSSFWorkbook(inputStream);
        Sheet sheet = workbook.getSheet("NullTest");
        Row dataRow = sheet.getRow(1);

        assertEquals(org.apache.poi.ss.usermodel.CellType.BLANK, dataRow.getCell(1).getCellType());
        assertEquals(org.apache.poi.ss.usermodel.CellType.BLANK, dataRow.getCell(5).getCellType());

        assertEquals("", dataRow.getCell(4).getStringCellValue());

        workbook.close();
    }
}