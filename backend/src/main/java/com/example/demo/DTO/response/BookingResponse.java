package com.example.demo.DTO.response;

import com.example.demo.model.Account;
import com.example.demo.model.PaymentMethod;
import com.example.demo.model.Screening;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingResponse {
    private Integer id;
    private Account account;
    private Screening screening;
    private PaymentMethod paymentMethod;
    private String promotionCodeApplied;
    private String discountTypeApplied;
    private BigDecimal discountApplied;
    private LocalDateTime bookingTime;
    private BigDecimal totalAmount;
    private String bookingStatus;
    private Integer seatCount;
    private List<BookedSeatResponse> bookedSeats; // Thêm để trả về thông tin ghế
}