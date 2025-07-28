package com.example.demo.service;

import com.example.demo.DTO.request.booking.BookingRequestDTO;
import com.example.demo.DTO.response.booking.BookingDetailResponseDTO;
import com.example.demo.DTO.response.dashboard.BookingTicketRecentlyResponse;
import com.example.demo.DTO.response.dashboard.DailyTicketRevenueResponse;
import com.example.demo.configuration.VnpayConfig;
import com.example.demo.exception.AppException;
import com.example.demo.exception.InternalServerException;
import com.example.demo.exception.NotFoundException;
import com.example.demo.exception.booking.*;
import com.example.demo.model.*;
import com.example.demo.repository.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

// mail confirm
import org.thymeleaf.context.Context;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class BookingService {
    private final BookingRepository bookingRepository;
    private final AccountRepository accountRepository;
    private final ScreeningRepository screeningRepository;
    private final SeatRepository seatRepository;
    private final PaymentMethodRepository paymentMethodRepository;
    private final BookedSeatRepository bookedSeatRepository;
    private final VnpayConfig vnpayConfig;
    private final EmailService emailService;
    private final PromotionService promotionService;
    private static final Logger logger = LoggerFactory.getLogger(BookingService.class);
    private static final BigDecimal POINTS_EARNING_RATE = new BigDecimal("0.04"); // 4%
    public static final int BOOKING_EXPIRATION_MINUTES = 10;
    public Long getTotalRevenueByStatus(String status) {
        return bookingRepository.getTotalRevenueByStatus(status)
                .orElseThrow(() -> new AppException("No revenue data found for status: " + status));
    }

    public List<DailyTicketRevenueResponse> getDailyTicketRevenue(int dailyCount) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime fromDate = now.minusDays(dailyCount).toLocalDate().atStartOfDay();
        LocalDateTime toDate = now.toLocalDate().atStartOfDay();
        List<DailyTicketRevenueResponse> stats = bookingRepository.getDailyTicketRevenue(fromDate, toDate);
        // Map day -> data
        Map<LocalDateTime, DailyTicketRevenueResponse> map = stats.stream()
                .collect(Collectors.toMap(DailyTicketRevenueResponse::getDate, dto -> dto));
        // Fill in the full date if there is no revenue on that date (revenue = 0)
        List<DailyTicketRevenueResponse> result = new ArrayList<>();
        for (int i = 0; i < dailyCount; i++) {
            LocalDateTime date = fromDate.plusDays(i).toLocalDate().atStartOfDay();
            DailyTicketRevenueResponse resp = map.getOrDefault(
                    date,
                    new DailyTicketRevenueResponse(Timestamp.valueOf(date), BigDecimal.ZERO, 0L));
            result.add(resp);
        }
        return result;
    }

    public List<DailyTicketRevenueResponse> getWeeklyTicketRevenue(int weekCount) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime fromDate = now.minusWeeks(weekCount).with(DayOfWeek.MONDAY).toLocalDate().atStartOfDay();
        LocalDateTime toDate = now.with(DayOfWeek.MONDAY).toLocalDate().atStartOfDay();
        List<DailyTicketRevenueResponse> stats = bookingRepository.getWeeklyRevenueAndTickets(fromDate, toDate);
        // Map week -> data
        Map<LocalDateTime, DailyTicketRevenueResponse> map = stats.stream()
                .collect(Collectors.toMap(DailyTicketRevenueResponse::getDate, dto -> dto));
        // Fill in the full week if there is no revenue on that week (revenue = 0)
        List<DailyTicketRevenueResponse> result = new ArrayList<>();
        for (int i = 0; i < weekCount; i++) {
            LocalDateTime weekStart = fromDate.plusWeeks(i).with(DayOfWeek.MONDAY).toLocalDate().atStartOfDay();
            DailyTicketRevenueResponse resp = map.getOrDefault(
                    weekStart,
                    new DailyTicketRevenueResponse(Timestamp.valueOf(weekStart), BigDecimal.ZERO, 0L));
            result.add(resp);
        }
        return result;
    }

    public List<DailyTicketRevenueResponse> getMonthlyTicketRevenue(int monthCount) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime fromDate = now.minusMonths(monthCount).withDayOfMonth(1).toLocalDate().atStartOfDay();
        LocalDateTime toDate = now.withDayOfMonth(1).toLocalDate().atStartOfDay();
        List<DailyTicketRevenueResponse> stats = bookingRepository.getMonthlyRevenueAndTickets(fromDate, toDate);
        // Map month -> data
        Map<LocalDateTime, DailyTicketRevenueResponse> map = stats.stream()
                .collect(Collectors.toMap(DailyTicketRevenueResponse::getDate, dto -> dto));
        // Fill in the full month if there is no revenue on that month (revenue = 0)
        List<DailyTicketRevenueResponse> result = new ArrayList<>();
        for (int i = 0; i < monthCount; i++) {
            LocalDateTime monthStart = fromDate.plusMonths(i).withDayOfMonth(1).toLocalDate().atStartOfDay();
            DailyTicketRevenueResponse resp = map.getOrDefault(
                    monthStart,
                    new DailyTicketRevenueResponse(Timestamp.valueOf(monthStart), BigDecimal.ZERO, 0L));
            result.add(resp);
        }
        return result;
    }

    public List<BookingTicketRecentlyResponse> getRecentlyBookedTickets(int limit) {
        return bookingRepository.getBookingTicketRecently(limit);
    }

    //-------------------booking

    private String generateUniqueBookingCode() {
        String code;
        do {
            String randomPart = UUID.randomUUID().toString().substring(30).toUpperCase();
            code = "CINE-" + randomPart;
        } while (bookingRepository.existsByBookingCode(code)); // Lặp lại nếu mã đã tồn tại
        return code;
    }

    @Transactional
    public BookingDetailResponseDTO createBooking(BookingRequestDTO request, Long accountId, HttpServletRequest httpServletRequest) {
        Optional<Booking> existingPendingBooking = bookingRepository.findByAccountAccountIdAndScreeningIdAndBookingStatus(accountId, request.getScreeningId(), "PENDING_PAYMENT");
        if (existingPendingBooking.isPresent()) {
            Booking booking = existingPendingBooking.get();
            if (booking.getBookingTime().plusMinutes(BOOKING_EXPIRATION_MINUTES).isAfter(LocalDateTime.now())) {
                throw new PendingBookingExistsException("Bạn đã có một đơn đặt vé đang chờ thanh toán cho suất chiếu này. Vui lòng hoàn tất hoặc hủy đơn hàng cũ.", booking.getId() );
            }
        }

        // 1. INPUT CHECK
        Account account = accountRepository.findWithLockingByAccountId(accountId)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy tài khoản."));
        Screening screening = screeningRepository.findById(request.getScreeningId())
                .orElseThrow(() -> new NotFoundException("Không tìm thấy suất chiếu."));
        PaymentMethod paymentMethod = paymentMethodRepository.findByIdAndActiveTrue(request.getPaymentMethodId())
                .orElseThrow(() -> new NotFoundException("Phương thức thanh toán không hợp lệ."));

        if (screening.getShowDateTime().isBefore(LocalDateTime.now())) {
            throw new BookingValidationException("Không thể đặt vé cho suất chiếu đã qua.");
        }
        if (request.getSeatIds() == null || request.getSeatIds().isEmpty()) {
            throw new BookingValidationException("Vui lòng chọn ít nhất một ghế.");
        }
        List<Seat> selectedSeats = seatRepository.findBySeatIdIn(request.getSeatIds());
        if (selectedSeats.size() != request.getSeatIds().size()) {
            throw new NotFoundException("Một hoặc nhiều ghế bạn chọn không tồn tại.");
        }
        Set<Long> alreadyBookedSeatIds = seatRepository.findBookedSeatIdsByScreeningId(screening.getId());
        for (Seat seat : selectedSeats) {
            if (alreadyBookedSeatIds.contains(seat.getSeatId())) {
                throw new SeatAlreadyBookedException("Ghế " + seat.getSeatRow() + seat.getSeatCol() + " đã được đặt hoặc đang được giữ.");
            }
            if (!seat.getCinemaRoom().getCinemaRoomId().equals(screening.getCinemaRoom().getCinemaRoomId())) {
                throw new BookingValidationException("Dữ liệu ghế không hợp lệ. Vui lòng thử lại.");
            }
            if ("Unavailable".equalsIgnoreCase(seat.getSeatStatus())) {
                throw new BookingValidationException("Ghế " + seat.getSeatRow() + seat.getSeatCol() + " không khả dụng để đặt.");
            }
        }
        validateSeatSelection(screening, selectedSeats);
        // 2. CALCULATE TOTAL PRINCIPAL
        BigDecimal originalTotalAmount = calculateOriginalTotalAmount(screening, selectedSeats);
        // 3. APPLY PROMOTION

        Promotion promotion = null;
        BigDecimal discountAmount = BigDecimal.ZERO;
        String promotionCodeApplied = null;
        String discountTypeApplied = null;

        if (request.getPromotionCode() != null && !request.getPromotionCode().isEmpty()) {
            // Gọi đến PromotionService để tìm và xác thực mã khuyến mãi
            promotion = promotionService.findAndValidatePromotion(request.getPromotionCode());

            // Nếu mã hợp lệ, tiếp tục kiểm tra điều kiện của đơn hàng
            if (originalTotalAmount.compareTo(promotion.getMinOrder()) < 0) {
                throw new InvalidPromotionException("Đơn hàng chưa đạt giá trị tối thiểu để áp dụng mã này.");
            }

            // Nếu tất cả điều kiện đều ổn, tiến hành tính toán giảm giá
            promotionCodeApplied = promotion.getCode();
            discountTypeApplied = promotion.getDiscountType();

            if ("PERCENT".equalsIgnoreCase(promotion.getDiscountType())) {
                discountAmount = originalTotalAmount
                        .multiply(promotion.getDiscountLevel().divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP));
                if (promotion.getMaxDiscount() != null && discountAmount.compareTo(promotion.getMaxDiscount()) > 0) {
                    discountAmount = promotion.getMaxDiscount();
                }
            } else if ("AMOUNT".equalsIgnoreCase(promotion.getDiscountType())) {
                discountAmount = promotion.getDiscountLevel();
            }

            if (discountAmount.compareTo(originalTotalAmount) > 0) {
                discountAmount = originalTotalAmount;
            }
        }
        BigDecimal finalTotalAmountAfterPromotion = originalTotalAmount.subtract(discountAmount);


        // 4. USE POIN
        BigDecimal pointsDiscountAmount = BigDecimal.ZERO;
        int pointsUsed = 0;
        if (request.getPointsToUse() > 0) {
            if (request.getPointsToUse() > account.getScore()) {
                throw new InsufficientPointsException("Số điểm sử dụng vượt quá số điểm hiện có.");
            }
            int pointsToUseRounded = (request.getPointsToUse() / 1000) * 1000;
            pointsDiscountAmount = new BigDecimal(pointsToUseRounded);
            if (pointsDiscountAmount.compareTo(finalTotalAmountAfterPromotion) > 0) {
                int maxPointsToUse = (finalTotalAmountAfterPromotion.intValue() / 1000) * 1000;
                pointsDiscountAmount = new BigDecimal(maxPointsToUse);
                pointsUsed = maxPointsToUse;
            } else {
                pointsUsed = pointsToUseRounded;
            }
            account.setScore(account.getScore() - pointsUsed);
        }
        BigDecimal finalAmountToPay = finalTotalAmountAfterPromotion.subtract(pointsDiscountAmount);

        // 5. CREATE BOOKING AND PROCESS PAYMENT
        Booking booking = new Booking();
        booking.setBookingCode(generateUniqueBookingCode());
        booking.setAccount(account);
        booking.setScreening(screening);
        booking.setPromotion(promotion);
        booking.setPaymentMethod(paymentMethod);
        booking.setPromotionCodeApplied(promotionCodeApplied);
        booking.setDiscountTypeApplied(discountTypeApplied);
        booking.setDiscountApplied(discountAmount);
        booking.setTotalAmount(finalAmountToPay);
        booking.setPointsUsed(pointsUsed);
        booking.setPointsDiscount(pointsDiscountAmount);

        String paymentUrl = null;
        if ("VNPAY".equalsIgnoreCase(paymentMethod.getName())) {
            booking.setBookingStatus("PENDING_PAYMENT");
            String vnpTxnRef = booking.getBookingCode() + "-" + System.currentTimeMillis();
            booking.setVnpTxnRef(vnpTxnRef);
            if (finalAmountToPay.compareTo(BigDecimal.ZERO) > 0) {
                paymentUrl = createVnpayPaymentUrl(finalAmountToPay, vnpTxnRef, screening.getMovie().getNameVN(),
                        httpServletRequest);
            } else {
                booking.setBookingStatus("PAID");
            }
        } else {
            throw new BookingValidationException("Phương thức thanh toán không được hỗ trợ: " + paymentMethod.getName());
        }

        Booking savedBooking = bookingRepository.save(booking);
        // 6. SAVE DETAILS OF BOOKED SEATS
        saveBookedSeats(savedBooking, selectedSeats, screening, discountAmount, pointsDiscountAmount);

        if ("PAID".equals(savedBooking.getBookingStatus())) {
            addPointsToAccount(savedBooking);
            sendBookingConfirmationEmail(savedBooking);
        }

        return convertToBookingDetailResponseDTO(savedBooking, paymentUrl, originalTotalAmount);
    }

    @Transactional
    public String handleVnpayReturn(Map<String, String> vnpayParams) {
        String vnp_SecureHash_FromVNPay = vnpayParams.remove("vnp_SecureHash");
        if (vnp_SecureHash_FromVNPay == null) {
            logger.error("VNPAY return missing vnp_SecureHash. Params: {}", vnpayParams);
            return vnpayConfig.getFrontendFailureUrl() + "?reason=missing_secure_hash";
        }
        String calculatedSecureHash = VnpayConfig.hashAllFields(vnpayParams, vnpayConfig.getVnp_HashSecret());
        String vnp_TxnRef = vnpayParams.get("vnp_TxnRef");
        if (vnp_TxnRef == null) {
            logger.error("VNPAY return missing vnp_TxnRef. Params: {}", vnpayParams);
            return vnpayConfig.getFrontendFailureUrl() + "?reason=missing_transaction_ref";
        }
        Optional<Booking> bookingOptional = bookingRepository.findByVnpTxnRef(vnp_TxnRef);
        if (!bookingOptional.isPresent()) {
            logger.warn("Booking not found for vnp_TxnRef: {}. Params: {}", vnp_TxnRef, vnpayParams);
            return vnpayConfig.getFrontendFailureUrl() + "?reason=booking_not_found&txnRef=" + vnp_TxnRef;
        }

        Booking booking = bookingOptional.get();
        String movieIdParam = (booking.getScreening() != null && booking.getScreening().getMovie() != null)
                ? "&movieId=" + booking.getScreening().getMovie().getId()
                : "";

        if (!"PENDING_PAYMENT".equals(booking.getBookingStatus())) {
            if ("PAID".equals(booking.getBookingStatus())) {
                return vnpayConfig.getFrontendSuccessUrl() + "?bookingId=" + booking.getId() + movieIdParam;
            }
            return vnpayConfig.getFrontendFailureUrl() + "?reason=booking_already_processed&bookingId="
                    + booking.getId() + movieIdParam;
        }

        if (calculatedSecureHash.equals(vnp_SecureHash_FromVNPay)) {
            String vnp_ResponseCode = vnpayParams.get("vnp_ResponseCode");
            if ("00".equals(vnp_ResponseCode)) {
                booking.setBookingStatus("PAID");
                addPointsToAccount(booking);
                bookingRepository.save(booking);
                sendBookingConfirmationEmail(booking);
                // No need to save booking again because addPointsToAccount has saved the account,
            } else {
                booking.setBookingStatus("PAYMENT_FAILED");
                refundPoints(booking);
            }
            bookingRepository.save(booking);

            if ("PAID".equals(booking.getBookingStatus())) {
                return vnpayConfig.getFrontendSuccessUrl() + "?bookingId=" + booking.getId() + movieIdParam;
            } else {
                return vnpayConfig.getFrontendFailureUrl() + "?reason=payment_declined&bookingId=" + booking.getId()
                        + "&vnp_ResponseCode=" + vnp_ResponseCode + movieIdParam;
            }
        } else {
            logger.error("SecureHash Mismatch for Booking ID: {}", booking.getId());
            booking.setBookingStatus("PAYMENT_FAILED");
            refundPoints(booking);
            bookingRepository.save(booking);
            return vnpayConfig.getFrontendFailureUrl() + "?reason=invalid_signature&bookingId=" + booking.getId()
                    + movieIdParam;
        }
    }


    private BigDecimal calculateOriginalTotalAmount(Screening screening, List<Seat> selectedSeats) {
        BigDecimal totalAmount = BigDecimal.ZERO;
        FareType fareType = screening.getFareType();
        BigDecimal basePrice = fareType.getBasePrice();
        BigDecimal movieFormatSurcharge = getMovieFormatSurcharge(fareType.getMovieFormat());
        BigDecimal timeSlotSurcharge = getTimeSlotSurcharge(fareType.getTimeSlotType());
        for (Seat seat : selectedSeats) {
            BigDecimal seatPrice = new BigDecimal(basePrice.toString())
                    .add(movieFormatSurcharge)
                    .add(timeSlotSurcharge);
            if (seat.getSeatType() != null && seat.getSeatType().getSeatTypePrice() != null) {
                seatPrice = seatPrice.add(seat.getSeatType().getSeatTypePrice());
            }
            totalAmount = totalAmount.add(seatPrice);
        }
        return totalAmount;
    }

    private void saveBookedSeats(Booking booking, List<Seat> selectedSeats, Screening screening,
            BigDecimal promotionDiscount, BigDecimal pointsDiscount) {
        FareType fareType = screening.getFareType();
        BigDecimal basePrice = fareType.getBasePrice();
        BigDecimal movieFormatSurcharge = getMovieFormatSurcharge(fareType.getMovieFormat());
        BigDecimal timeSlotSurcharge = getTimeSlotSurcharge(fareType.getTimeSlotType());
        BigDecimal totalDiscount = promotionDiscount.add(pointsDiscount);
        BigDecimal discountPerSeat = (selectedSeats.isEmpty()) ? BigDecimal.ZERO
                : totalDiscount.divide(new BigDecimal(selectedSeats.size()), 2, RoundingMode.HALF_UP);

        List<BookedSeat> bookedSeatEntities = new ArrayList<>();
        for (Seat seat : selectedSeats) {
            BigDecimal originalSeatPrice = new BigDecimal(basePrice.toString())
                    .add(movieFormatSurcharge)
                    .add(timeSlotSurcharge);
            if (seat.getSeatType() != null && seat.getSeatType().getSeatTypePrice() != null) {
                originalSeatPrice = originalSeatPrice.add(seat.getSeatType().getSeatTypePrice());
            }
            BigDecimal pricePaidForThisSeat = originalSeatPrice.subtract(discountPerSeat);
            if (pricePaidForThisSeat.compareTo(BigDecimal.ZERO) < 0) {
                pricePaidForThisSeat = BigDecimal.ZERO;
            }
            BookedSeat bookedSeat = new BookedSeat();
            bookedSeat.setBooking(booking);
            bookedSeat.setSeat(seat);
            bookedSeat.setPricePaid(pricePaidForThisSeat);
            bookedSeatEntities.add(bookedSeat);
        }
        bookedSeatRepository.saveAll(bookedSeatEntities);
    }

    private void addPointsToAccount(Booking booking) {
        if (booking == null || !"PAID".equals(booking.getBookingStatus()))
            return;
        Account account = accountRepository.findWithLockingByAccountId(booking.getAccount().getAccountId())
                .orElse(null);
        if (account == null) {
            logger.warn("Account not found for adding points. Booking ID: {}", booking.getId());
            return;
        }
        BigDecimal amountPaid = booking.getTotalAmount();
        if (amountPaid != null && amountPaid.compareTo(BigDecimal.ZERO) > 0) {
            int pointsEarned = amountPaid.multiply(POINTS_EARNING_RATE).intValue();
            account.setScore(account.getScore() + pointsEarned);
            logger.info("Added {} points to account ID {}. New score: {}", pointsEarned, account.getAccountId(),
                    account.getScore());
        }
    }

    private void refundPoints(Booking booking) {
        if (booking == null || booking.getPointsUsed() == null || booking.getPointsUsed() <= 0) {
            return;
        }
        Account account = accountRepository.findWithLockingByAccountId(booking.getAccount().getAccountId())
                .orElse(null);
        if (account == null) {
            logger.warn("Account not found for refunding points. Booking ID: {}", booking.getId());
            return;
        }
        account.setScore(account.getScore() + booking.getPointsUsed());
        logger.info("Refunded {} points to account ID {} due to failed/cancelled payment.", booking.getPointsUsed(),
                account.getAccountId());
    }

    private BigDecimal getMovieFormatSurcharge(String movieFormat) {
        if ("3D".equalsIgnoreCase(movieFormat))
            return new BigDecimal("20000");
        if ("IMAX".equalsIgnoreCase(movieFormat))
            return new BigDecimal("35000");
        return BigDecimal.ZERO;
    }

    private BigDecimal getTimeSlotSurcharge(String timeSlotType) {
        if ("Cuối Tuần".equalsIgnoreCase(timeSlotType))
            return new BigDecimal("10000");
        if ("Ngày Lễ".equalsIgnoreCase(timeSlotType))
            return new BigDecimal("15000");
        return BigDecimal.ZERO;
    }

    private String createVnpayPaymentUrl(BigDecimal amount, String vnpTxnRef, String orderInfoDesc,
            HttpServletRequest req) {
        long amountLong = amount.multiply(BigDecimal.valueOf(100)).longValue();

        Map<String, String> vnp_Params = new TreeMap<>();
        vnp_Params.put("vnp_Version", VnpayConfig.VNP_VERSION);
        vnp_Params.put("vnp_Command", VnpayConfig.VNP_COMMAND_PAY);
        vnp_Params.put("vnp_TmnCode", vnpayConfig.getVnp_TmnCode());
        vnp_Params.put("vnp_Amount", String.valueOf(amountLong));
        vnp_Params.put("vnp_CurrCode", VnpayConfig.VNP_CURRCODE);
        vnp_Params.put("vnp_TxnRef", vnpTxnRef);
        String vnp_OrderInfo = "Thanh toan don hang: " + vnpTxnRef + ". Noi dung: " + orderInfoDesc;
        vnp_Params.put("vnp_OrderInfo", vnp_OrderInfo);
        vnp_Params.put("vnp_OrderType", VnpayConfig.VNP_ORDERTYPE);
        vnp_Params.put("vnp_Locale", VnpayConfig.VNP_LOCALE);
        vnp_Params.put("vnp_ReturnUrl", vnpayConfig.getVnp_ReturnUrl());
        vnp_Params.put("vnp_IpAddr", VnpayConfig.getIpAddress(req));

        Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        String vnp_CreateDate = formatter.format(cld.getTime());
        vnp_Params.put("vnp_CreateDate", vnp_CreateDate);

        cld.add(Calendar.MINUTE, 15);
        String vnp_ExpireDate = formatter.format(cld.getTime());
        vnp_Params.put("vnp_ExpireDate", vnp_ExpireDate);

        // Build hashData, queryUrl
        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();

        for (Map.Entry<String, String> entry : vnp_Params.entrySet()) {
            String fieldName = entry.getKey();
            String fieldValue = entry.getValue();
            if ((fieldValue != null) && (fieldValue.length() > 0)) {
                if (hashData.length() > 0) {
                    hashData.append('&');
                }
                hashData.append(fieldName);
                hashData.append('=');
                try {
                    hashData.append(URLEncoder.encode(fieldValue, StandardCharsets.UTF_8.toString()));
                } catch (UnsupportedEncodingException e) {
                    throw new InternalServerException("Lỗi hệ thống khi tạo URL thanh toán.", e);
                }
                if (query.length() > 0) {
                    query.append('&');
                }
                try {
                    query.append(URLEncoder.encode(fieldName, StandardCharsets.UTF_8.toString()));
                    query.append('=');
                    query.append(URLEncoder.encode(fieldValue, StandardCharsets.UTF_8.toString()));
                } catch (UnsupportedEncodingException e) {
                    throw new InternalServerException("Lỗi hệ thống khi tạo URL thanh toán.", e);
                }
            }
        }
        String queryUrl = query.toString();
        String vnp_SecureHash = VnpayConfig.hmacSHA512(vnpayConfig.getVnp_HashSecret(), hashData.toString());
        System.out.println("createVnpayPaymentUrl - String to hash (for sending): " + hashData.toString());
        System.out.println("createVnpayPaymentUrl - SecureHash (for sending): " + vnp_SecureHash);

        queryUrl += "&vnp_SecureHash=" + vnp_SecureHash;
        return vnpayConfig.getVnp_PayUrl() + "?" + queryUrl;
    }

    @Transactional
    public List<BookingDetailResponseDTO> getUserBookingHistory(Long accountId) {
        List<Booking> bookings = bookingRepository.findByAccountAccountIdOrderByBookingTimeDesc(accountId);
        return bookings.stream()
                .map(b -> convertToBookingDetailResponseDTO(b, null, calculateOriginalAmount(b)))
                .collect(Collectors.toList());
    }

    public BookingDetailResponseDTO getBookingDetailsForUser(Integer bookingId, Long accountId) {
        Booking booking = bookingRepository.findByIdAndAccountAccountId(bookingId, accountId)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy đơn đặt vé này hoặc bạn không có quyền truy cập."));
        return convertToBookingDetailResponseDTO(booking, null, calculateOriginalAmount(booking));
    }

    private BigDecimal calculateOriginalAmount(Booking booking) {
        // Total principal = payment + total discount
        BigDecimal totalDiscount = BigDecimal.ZERO;
        if (booking.getDiscountApplied() != null) {
            totalDiscount = totalDiscount.add(booking.getDiscountApplied());
        }
        if (booking.getPointsDiscount() != null) {
            totalDiscount = totalDiscount.add(booking.getPointsDiscount());
        }
        return booking.getTotalAmount().add(totalDiscount);
    }

    private BookingDetailResponseDTO convertToBookingDetailResponseDTO(Booking booking, String paymentUrl,
            BigDecimal originalAmount) {
        if (booking == null)
            return null;
        int pointsEarned = 0;
        // Chỉ tính điểm thưởng cho các booking đã thanh toán thành công
        if ("PAID".equals(booking.getBookingStatus()) && booking.getTotalAmount().compareTo(BigDecimal.ZERO) > 0) {
            pointsEarned = booking.getTotalAmount().multiply(POINTS_EARNING_RATE).intValue();
        }
        BookingDetailResponseDTO.AccountInfoDTO accountInfo = null;
        if (booking.getAccount() != null) {
            accountInfo = BookingDetailResponseDTO.AccountInfoDTO.builder()
                    .accountId(booking.getAccount().getAccountId())
                    .email(booking.getAccount().getEmail())
                    .fullName(booking.getAccount().getFullName())
                    .phoneNumber(booking.getAccount().getPhoneNumber())
                    .build();
        }

        BookingDetailResponseDTO.ScreeningInfoDTO screeningInfo = null;
        if (booking.getScreening() != null) {
            Screening s = booking.getScreening();
            screeningInfo = BookingDetailResponseDTO.ScreeningInfoDTO.builder()
                    .screeningId(s.getId())
                    .movieNameVn(s.getMovie() != null ? s.getMovie().getNameVN() : "N/A")
                    .movieNameEn(s.getMovie() != null ? s.getMovie().getNameEN() : "N/A")
                    .cinemaRoomName(s.getCinemaRoom() != null ? s.getCinemaRoom().getCinemaRoomName() : "N/A")
                    .showDateTime(s.getShowDateTime())
                    .movieFormat(s.getFareType() != null ? s.getFareType().getMovieFormat() : "N/A")
                    .build();
        }

        BookingDetailResponseDTO.PromotionInfoDTO promotionInfo = null;
        if (booking.getPromotion() != null) {
            Promotion p = booking.getPromotion();
            promotionInfo = BookingDetailResponseDTO.PromotionInfoDTO.builder()
                    .promotionId(p.getId())
                    .code(p.getCode())
                    .build();
        }

        BookingDetailResponseDTO.PaymentMethodInfoDTO paymentMethodInfo = null;
        if (booking.getPaymentMethod() != null) {
            PaymentMethod pm = booking.getPaymentMethod();
            paymentMethodInfo = BookingDetailResponseDTO.PaymentMethodInfoDTO.builder()
                    .paymentMethodId(pm.getId())
                    .methodName(pm.getName())
                    .build();
        }

        List<BookingDetailResponseDTO.BookedSeatInfoDTO> bookedSeatInfoList = Collections.emptyList();
        if (booking.getBookedSeats() != null && !booking.getBookedSeats().isEmpty()) {
            bookedSeatInfoList = booking.getBookedSeats().stream().map(bs -> {
                Seat seat = bs.getSeat();
                return BookingDetailResponseDTO.BookedSeatInfoDTO.builder()
                        .bookedSeatId(bs.getId())
                        .seatId(seat.getSeatId())
                        .seatRow(seat.getSeatRow())
                        .seatCol(seat.getSeatCol())
                        .seatTypeName(seat.getSeatType() != null ? seat.getSeatType().getSeatTypeName() : "Standard")
                        .pricePaid(bs.getPricePaid())
                        .build();
            }).collect(Collectors.toList());
        }

        return BookingDetailResponseDTO.builder()
                .bookingId(booking.getId())
                .bookingCode(booking.getBookingCode())
                .account(accountInfo)
                .screening(screeningInfo)
                .promotion(promotionInfo)
                .paymentMethod(paymentMethodInfo)
                .promotionCodeApplied(booking.getPromotionCodeApplied())
                .discountTypeApplied(booking.getDiscountTypeApplied())
                .discountApplied(booking.getDiscountApplied())
                .pointsUsed(booking.getPointsUsed())
                .pointsDiscount(booking.getPointsDiscount())
                .pointsEarned(pointsEarned)
                .bookingTime(booking.getBookingTime())
                .totalAmount(booking.getTotalAmount())
                .originalAmount(originalAmount)
                .bookingStatus(booking.getBookingStatus())
                .bookedSeats(bookedSeatInfoList)
                .paymentUrl(paymentUrl)
                .build();
    }

    private void sendBookingConfirmationEmail(Booking booking) {
        if (booking == null || booking.getAccount() == null) {
            logger.warn("Cannot send confirmation email. Booking or account is null.");
            return;
        }

        try {
            Context context = new Context();
            context.setVariable("subject", "Xác nhận đặt vé thành công!");
            context.setVariable("customerName", booking.getAccount().getFullName());
            context.setVariable("bookingCode", booking.getBookingCode());
            context.setVariable("movieName", booking.getScreening().getMovie().getNameVN());

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm - EEEE, dd/MM/yyyy",
                    Locale.forLanguageTag("vi-VN"));
            context.setVariable("showTime", booking.getScreening().getShowDateTime().format(formatter));

            context.setVariable("roomAndFormat",
                    String.format("%s / %s",
                            booking.getScreening().getCinemaRoom().getCinemaRoomName(),
                            booking.getScreening().getFareType().getMovieFormat()));

            String seats = booking.getBookedSeats().stream()
                    .map(bs -> bs.getSeat().getSeatRow() + bs.getSeat().getSeatCol())
                    .collect(Collectors.joining(", "));
            context.setVariable("seats", seats);

            context.setVariable("totalAmount", String.format("%,.0f", booking.getTotalAmount()));
            String qrContent = "Booking Code: " + booking.getBookingCode();
            byte[] qrCodeBytes = generateQrCodeImage(qrContent, 200, 200);

            String qrCodeImageCid = "qrCodeImage"; // Content-ID này phải khớp với cid: trong HTML
            context.setVariable("qrCodeImageCid", qrCodeImageCid);
            emailService.sendHtmlEmailWithInlineImage(
                    booking.getAccount().getEmail(),
                    "Xác nhận đặt vé thành công - Mã vé: " + booking.getBookingCode(),
                    "booking-confirmation", // Tên file template (không có .html)
                    context,
                    qrCodeImageCid,
                    qrCodeBytes,
                    "image/png");

            logger.info("Successfully sent booking confirmation email for booking code: {}", booking.getBookingCode());
        } catch (Exception e) {
            logger.error("Failed to send booking confirmation email for booking code: {}. Error: {}",
                    booking.getBookingCode(), e.getMessage());
        }
    }

    private byte[] generateQrCodeImage(String text, int width, int height) throws Exception {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, width, height);
        ByteArrayOutputStream pngOutputStream = new ByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(bitMatrix, "PNG", pngOutputStream);
        return pngOutputStream.toByteArray();
    }

    //Check
    private void validateSeatSelection(Screening screening, List<Seat> selectedSeats) {
        Map<String, List<Seat>> allSeatsByRow = seatRepository.findByCinemaRoom(screening.getCinemaRoom()).stream()
                .sorted(Comparator.comparing(Seat::getSeatRow).thenComparing(s -> Integer.parseInt(s.getSeatCol())))
                .collect(Collectors.groupingBy(Seat::getSeatRow, LinkedHashMap::new, Collectors.toList()));
        Set<Long> occupiedSeatIds = new HashSet<>(seatRepository.findBookedSeatIdsByScreeningId(screening.getId()));
        selectedSeats.forEach(s -> occupiedSeatIds.add(s.getSeatId()));
        for (List<Seat> rowSeats : allSeatsByRow.values()) {
            for (int i = 0; i < rowSeats.size(); i++) {
                Seat currentSeat = rowSeats.get(i);
                if (!occupiedSeatIds.contains(currentSeat.getSeatId())) {
                    if (currentSeat.getSeatType() != null && "couple".equalsIgnoreCase(currentSeat.getSeatType().getSeatTypeName())) {
                        continue;
                    }
                    boolean isLeftNeighborOccupied = (i == 0) || occupiedSeatIds.contains(rowSeats.get(i - 1).getSeatId());
                    boolean isRightNeighborOccupied = (i == rowSeats.size() - 1) || occupiedSeatIds.contains(rowSeats.get(i + 1).getSeatId());

                    if (isLeftNeighborOccupied && isRightNeighborOccupied) {
                        logger.warn("Invalid seat selection: creates a single empty seat gap at {}{}", currentSeat.getSeatRow(), currentSeat.getSeatCol());
                        throw new BookingValidationException("Không thể để lại một ghế trống duy nhất. Vui lòng chọn ghế liền kề.");
                    }
                }
            }
        }
    }

    @Transactional
    public String retryPayment(Integer bookingId, Long accountId, HttpServletRequest httpServletRequest) {
        Booking booking = bookingRepository.findByIdAndAccountAccountId(bookingId, accountId)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy đơn đặt vé này."));

        if (!"PENDING_PAYMENT".equals(booking.getBookingStatus())) {
            throw new BookingValidationException("Vé này không còn ở trạng thái chờ thanh toán.");
        }

        if (booking.getBookingTime().plusMinutes(BOOKING_EXPIRATION_MINUTES).isBefore(LocalDateTime.now())) {
            booking.setBookingStatus("EXPIRED");
            refundPoints(booking);
            bookingRepository.save(booking);
            throw new BookingValidationException("Vé đã hết hạn thanh toán.");
        }

        // Create a new vnp_TxnRef for this payment attempt
        String newVnpTxnRef = booking.getBookingCode() + "-" + System.currentTimeMillis();
        booking.setVnpTxnRef(newVnpTxnRef);
        bookingRepository.save(booking);

        // New URL VNPAY
        return createVnpayPaymentUrl(
                booking.getTotalAmount(),
                newVnpTxnRef,
                booking.getScreening().getMovie().getNameVN(),
                httpServletRequest);
    }

    @Scheduled(cron = "0 */5 * * * *") //5 minute
    @Transactional
    public void cancelExpiredPendingBookings() {
        LocalDateTime expirationTime = LocalDateTime.now().minusMinutes(15);
        logger.info("Running scheduled task to cancel expired bookings older than {}", expirationTime);

        List<Booking> expiredBookings = bookingRepository.findAllByBookingStatusAndBookingTimeBefore("PENDING_PAYMENT",
                expirationTime);

        if (expiredBookings.isEmpty()) {
            logger.info("No expired pending bookings found.");
            return;
        }

        logger.warn("Found {} expired pending bookings to cancel.", expiredBookings.size());
        for (Booking booking : expiredBookings) {
            booking.setBookingStatus("EXPIRED");
            refundPoints(booking);
            bookingRepository.save(booking);
            logger.warn("Expired booking with code: {}", booking.getBookingCode());
        }
    }
    public List<BookingDetailResponseDTO> getAllBookingsByMovieId(Long movieId) {
        List<Booking> bookings = bookingRepository.findAllByMovieId(movieId);
        return bookings.stream()
                .map(b -> convertToBookingDetailResponseDTO(b, null, calculateOriginalAmount(b)))
                .collect(Collectors.toList());
    }
    public Long getTotalBookingsByMovieId(Long movieId) {
        return bookingRepository.countBookingsByMovieId(movieId);
    }
}