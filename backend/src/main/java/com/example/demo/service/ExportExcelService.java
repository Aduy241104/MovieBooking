package com.example.demo.service;

import com.example.demo.model.Booking;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ExportExcelService {

    public ByteArrayInputStream bookingsToExcel(List<Booking> bookings, String sheetName) throws IOException {
        String[] columns = {
                "Mã Vé", "Tên Phim", "Suất Chiếu", "Phòng Chiếu",
                "Ghế", "Tên Khách Hàng", "Email", "SĐT", "Tổng Tiền",
                "Trạng thái", "Phương thức TT", "Thời gian đặt"
        };

        try (
                Workbook workbook = new XSSFWorkbook();
                ByteArrayOutputStream out = new ByteArrayOutputStream();
        ) {
            Sheet sheet = workbook.createSheet(sheetName);

            // --- Create Header ---
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setColor(IndexedColors.BLUE.getIndex());

            CellStyle headerCellStyle = workbook.createCellStyle();
            headerCellStyle.setFont(headerFont);
            headerCellStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            Row headerRow = sheet.createRow(0);
            for (int col = 0; col < columns.length; col++) {
                Cell cell = headerRow.createCell(col);
                cell.setCellValue(columns[col]);
                cell.setCellStyle(headerCellStyle);
            }

            // --- Fill data into rows ---
            int rowIdx = 1;
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm dd-MM-yyyy");

            for (Booking booking : bookings) {
                Row row = sheet.createRow(rowIdx++);

                // Concatenate the chair names
                String seats = booking.getBookedSeats().stream()
                        .map(bs -> bs.getSeat().getSeatRow() + bs.getSeat().getSeatCol())
                        .collect(Collectors.joining(", "));

                row.createCell(0).setCellValue(booking.getBookingCode());
                row.createCell(1).setCellValue(booking.getScreening().getMovie().getNameVN());
                row.createCell(2).setCellValue(booking.getScreening().getShowDateTime().format(formatter));
                row.createCell(3).setCellValue(booking.getScreening().getCinemaRoom().getCinemaRoomName());
                row.createCell(4).setCellValue(seats);
                row.createCell(5).setCellValue(booking.getAccount().getFullName());
                row.createCell(6).setCellValue(booking.getAccount().getEmail());
                row.createCell(7).setCellValue(booking.getAccount().getPhoneNumber());
                row.createCell(8).setCellValue(booking.getTotalAmount().doubleValue());
                row.createCell(9).setCellValue(booking.getBookingStatus());
                row.createCell(10).setCellValue(booking.getPaymentMethod().getName());
                row.createCell(11).setCellValue(booking.getBookingTime().format(formatter));
            }

            // Auto adjust column width
            for(int i = 0; i < columns.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        }
    }
}