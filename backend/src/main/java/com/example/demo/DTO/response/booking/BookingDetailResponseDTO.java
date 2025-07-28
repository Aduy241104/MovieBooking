package com.example.demo.DTO.response.booking;

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
public class BookingDetailResponseDTO {
    private Integer bookingId;
    private String bookingCode;
    private AccountInfoDTO account;
    private ScreeningInfoDTO screening;
    private PromotionInfoDTO promotion;
    private PaymentMethodInfoDTO paymentMethod;
    private String promotionCodeApplied;
    private String discountTypeApplied;
    private BigDecimal discountApplied;
    private LocalDateTime bookingTime;
    private BigDecimal totalAmount;
    private BigDecimal originalAmount;
    private Integer pointsUsed;
    private BigDecimal pointsDiscount;
    private String bookingStatus;
    private List<BookedSeatInfoDTO> bookedSeats;
    private String paymentUrl;

    @Data
    @Builder
    public static class AccountInfoDTO {
        private Long accountId;
        private String email;
        private String fullName;
        private String phoneNumber;
    }

    @Data
    @Builder
    public static class ScreeningInfoDTO {
        private Long screeningId;
        private String movieNameVn;
        private String movieNameEn;
        private String cinemaRoomName;
        private LocalDateTime showDateTime;
        private String movieFormat;
    }

    @Data
    @Builder
    public static class PromotionInfoDTO {
        private Long promotionId;
        private String code;
        private String detail;
    }

    @Data
    @Builder
    public static class PaymentMethodInfoDTO {
        private Long paymentMethodId;
        private String methodName;
    }


    @Data
    @Builder
    public static class BookedSeatInfoDTO {
        private Long bookedSeatId;
        private Long seatId;
        private String seatRow;
        private String seatCol;
        private String seatTypeName;
        private BigDecimal pricePaid;
    }
}