package com.example.demo.service;

import com.example.demo.DTO.request.BookingRequestDTO;
import com.example.demo.DTO.response.booking.BookingDetailResponseDTO;
import com.example.demo.DTO.response.dashboard.BookingTicketRecentlyResponse;
import com.example.demo.DTO.response.dashboard.DailyTicketRevenueResponse;
import com.example.demo.configuration.VnpayConfig;
import com.example.demo.model.*;
import com.example.demo.repository.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookingService {
    private final BookingRepository bookingRepository;
    private final AccountRepository accountRepository;
    private final ScreeningRepository screeningRepository;
    private final SeatRepository seatRepository;
    private final PromotionRepository promotionRepository;
    private final PaymentMethodRepository paymentMethodRepository;
    private final BookedSeatRepository bookedSeatRepository;
    private final VnpayConfig vnpayConfig; // Inject VNPAY config
    private static final Logger logger = LoggerFactory.getLogger(BookingService.class);


    public Long getTotalRevenueByStatus(String status) {
        return bookingRepository.getTotalRevenueByStatus(status);
    }

    public List<DailyTicketRevenueResponse> getDailyTicketRevenue(int dailyCount) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime fromDate = now.minusDays(dailyCount).toLocalDate().atStartOfDay();
        LocalDateTime toDate = now.toLocalDate().atStartOfDay();

        List<Object[]> stats = bookingRepository.getDailyTicketRevenue(fromDate, toDate);

        // Map ngày -> dữ liệu
        Map<LocalDate, DailyTicketRevenueResponse> map = new HashMap<>();
        for (Object[] row : stats) {
            LocalDate date = ((java.sql.Timestamp) row[0]).toLocalDateTime().toLocalDate();
            Long revenue = ((Number) row[1]).longValue();
            Long tickets = ((Number) row[2]).longValue();
            map.put(date, new DailyTicketRevenueResponse(date, revenue, tickets));
        }
        // Fill đủ 7 ngày
        List<DailyTicketRevenueResponse> result = new ArrayList<>();
        for (int i = 0; i < dailyCount; i++) {
            LocalDate date = fromDate.plusDays(i).toLocalDate();
            DailyTicketRevenueResponse resp = map.getOrDefault(
                    date,
                    new DailyTicketRevenueResponse(date, 0L, 0L));
            result.add(resp);
        }
        return result;
    }

    public List<DailyTicketRevenueResponse> getWeeklyTicketRevenue(int weekCount) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime fromDate = now.minusWeeks(weekCount).with(java.time.DayOfWeek.MONDAY).toLocalDate()
                .atStartOfDay();
        LocalDateTime toDate = now.with(DayOfWeek.MONDAY).toLocalDate().atStartOfDay();

        List<Object[]> stats = bookingRepository.getWeeklyRevenueAndTickets(fromDate, toDate);

        Map<LocalDate, DailyTicketRevenueResponse> map = new HashMap<>();
        for (Object[] row : stats) {
            LocalDate weekStart = ((java.sql.Timestamp) row[0]).toLocalDateTime().toLocalDate();
            Long revenue = ((Number) row[1]).longValue();
            Long tickets = ((Number) row[2]).longValue();
            map.put(weekStart, new DailyTicketRevenueResponse(weekStart, revenue, tickets));
        }
        List<DailyTicketRevenueResponse> result = new ArrayList<>();
        for (int i = 0; i < weekCount; i++) {
            LocalDate weekStart = fromDate.plusWeeks(i).with(DayOfWeek.MONDAY).toLocalDate();
            DailyTicketRevenueResponse resp = map.getOrDefault(
                    weekStart,
                    new DailyTicketRevenueResponse(weekStart, 0L, 0L));
            result.add(resp);
        }
        return result;
    }

    public List<DailyTicketRevenueResponse> getMonthlyTicketRevenue(int monthCount) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime fromDate = now.minusMonths(monthCount).withDayOfMonth(1).toLocalDate().atStartOfDay();
        LocalDateTime toDate = now.withDayOfMonth(1).toLocalDate().atStartOfDay();

        List<Object[]> stats = bookingRepository.getMonthlyRevenueAndTickets(fromDate, toDate);

        Map<LocalDate, DailyTicketRevenueResponse> map = new HashMap<>();
        for (Object[] row : stats) {
            LocalDate monthStart = ((java.sql.Timestamp) row[0]).toLocalDateTime().toLocalDate();
            Long revenue = ((Number) row[1]).longValue();
            Long tickets = ((Number) row[2]).longValue();
            map.put(monthStart, new DailyTicketRevenueResponse(monthStart, revenue, tickets));
        }
        List<DailyTicketRevenueResponse> result = new ArrayList<>();
        for (int i = 0; i < monthCount; i++) {
            LocalDate monthStart = fromDate.plusMonths(i).withDayOfMonth(1).toLocalDate();
            DailyTicketRevenueResponse resp = map.getOrDefault(
                    monthStart,
                    new DailyTicketRevenueResponse(monthStart, 0L, 0L));
            result.add(resp);
        }
        return result;
    }

    public List<BookingTicketRecentlyResponse> getRecentlyBookedTickets(int limit) {
        List<Object[]> stats = bookingRepository.getBookingTicketRecently(limit);
        List<BookingTicketRecentlyResponse> result = new ArrayList<>();
        for (Object[] row : stats) {
            String fullName = (String) row[1];
            String email = (String) row[2];
            String movieTitle = (String) row[3];
            String cinemaRoomName = (String) row[4];
            LocalDateTime bookingDate = ((java.sql.Timestamp) row[5]).toLocalDateTime();
            int seatCount = ((Number) row[6]).intValue();
            long totalPrice = ((Number) row[7]).longValue();
            String paymentMethod = (String) row[8];
            String paymentStatus = (String) row[9];

            result.add(new BookingTicketRecentlyResponse(
                    fullName,
                    email,
                    movieTitle,
                    cinemaRoomName,
                    bookingDate,
                    seatCount,
                    totalPrice,
                    paymentMethod,
                    paymentStatus));
        }
        return result;
    }
    //booking

    @Transactional
    public BookingDetailResponseDTO createBooking(BookingRequestDTO request, Long accountId, HttpServletRequest httpServletRequest) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("Account not found"));
        Screening screening = screeningRepository.findById(request.getScreeningId())
                .orElseThrow(() -> new RuntimeException("Screening not found"));
        PaymentMethod paymentMethod = paymentMethodRepository.findByIdAndActiveTrue(request.getPaymentMethodId())
                .orElseThrow(() -> new RuntimeException("Payment method not found or not active"));

        if (screening.getShowDateTime().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Cannot book for a past screening.");
        }

        List<Seat> selectedSeats = seatRepository.findBySeatIdIn(request.getSeatIds());
        if (selectedSeats.size() != request.getSeatIds().size()) {
            throw new RuntimeException("One or more selected seats not found.");
        }

        // Kiểm tra ghế đã được đặt chưa
        Set<Long> alreadyBookedSeatIds = seatRepository.findBookedSeatIdsByScreeningId(screening.getId());
        for (Seat seat : selectedSeats) {
            if (alreadyBookedSeatIds.contains(seat.getSeatId())) {
                throw new RuntimeException("Seat " + seat.getSeatRow() + seat.getSeatCol() + " is already booked.");
            }
            if (!seat.getCinemaRoom().getCinemaRoomId().equals(screening.getCinemaRoom().getCinemaRoomId())) {
                throw new RuntimeException("Seat " + seat.getSeatRow() + seat.getSeatCol() + " does not belong to the screening's cinema room.");
            }
            if ("Unavailable".equalsIgnoreCase(seat.getSeatStatus())) {
                throw new RuntimeException("Seat " + seat.getSeatRow() + seat.getSeatCol() + " is unavailable.");
            }
        }

        // Tính toán tổng tiền gốc
        BigDecimal originalTotalAmount = BigDecimal.ZERO;
        FareType fareType = screening.getFareType();
        if (fareType == null || fareType.getBasePrice() == null) {
            throw new RuntimeException("Fare type or base price not configured for this screening.");
        }
        BigDecimal baseTicketPrice = fareType.getBasePrice();

        for (Seat seat : selectedSeats) {
            BigDecimal seatPrice = baseTicketPrice;
            if (seat.getSeatType() != null && seat.getSeatType().getSeatTypePrice() != null) {
                seatPrice = seatPrice.add(seat.getSeatType().getSeatTypePrice());
            }
            originalTotalAmount = originalTotalAmount.add(seatPrice);
        }

        // Áp dụng khuyến mãi
        Promotion promotion = null;
        BigDecimal discountAmount = BigDecimal.ZERO;
        String promotionCodeApplied = null;
        String discountTypeApplied = null;

        if (request.getPromotionCode() != null && !request.getPromotionCode().isEmpty()) {
            promotion = promotionRepository.findByCode(request.getPromotionCode());
            if (promotion == null || !promotion.getActive() || promotion.getIsDeleted() ||
                    LocalDateTime.now().isBefore(promotion.getStartTime()) || LocalDateTime.now().isAfter(promotion.getEndTime())) {
                throw new RuntimeException("Invalid or expired promotion code.");
            }
            if (originalTotalAmount.compareTo(promotion.getMinOrder()) < 0) {
                throw new RuntimeException("Order total does not meet promotion's minimum requirement.");
            }

            promotionCodeApplied = promotion.getCode();
            discountTypeApplied = promotion.getDiscountType();

            if ("PERCENT".equalsIgnoreCase(promotion.getDiscountType())) {
                discountAmount = originalTotalAmount.multiply(promotion.getDiscountLevel().divide(BigDecimal.valueOf(100)));
                if (promotion.getMaxDiscount() != null && discountAmount.compareTo(promotion.getMaxDiscount()) > 0) {
                    discountAmount = promotion.getMaxDiscount();
                }
            } else if ("AMOUNT".equalsIgnoreCase(promotion.getDiscountType())) {
                discountAmount = promotion.getDiscountLevel();
            }
            // Đảm bảo giảm giá không lớn hơn tổng tiền
            if (discountAmount.compareTo(originalTotalAmount) > 0) {
                discountAmount = originalTotalAmount;
            }
        }

        BigDecimal finalTotalAmount = originalTotalAmount.subtract(discountAmount);

        Booking booking = new Booking();
        booking.setAccount(account);
        booking.setScreening(screening);
        booking.setPromotion(promotion);
        booking.setPaymentMethod(paymentMethod);
        booking.setPromotionCodeApplied(promotionCodeApplied);
        booking.setDiscountTypeApplied(discountTypeApplied);
        booking.setDiscountApplied(discountAmount);
        booking.setTotalAmount(finalTotalAmount);
        // booking.setBookingTime(); // @PrePersist sẽ tự set

        // Tạo vnp_TxnRef duy nhất cho booking
        String vnp_TxnRef = VnpayConfig.getRandomNumber(8); // Hoặc một UUID
        booking.setVnpTxnRef(vnp_TxnRef);


        // Xử lý trạng thái booking dựa trên phương thức thanh toán
        String paymentUrl = null;
        if ("Tại quầy".equalsIgnoreCase(paymentMethod.getName())) {
            booking.setBookingStatus("RESERVED"); // Hoặc PENDING nếu cần nhân viên xác nhận
        } else if (Arrays.asList("Ví MoMo", "ZaloPay", "Thẻ tín dụng/Ghi nợ").contains(paymentMethod.getName())) {
            // Với VNPAY (giả định "Thẻ tín dụng/Ghi nợ" là VNPAY)
            if ("Thẻ tín dụng/Ghi nợ".equalsIgnoreCase(paymentMethod.getName())) {
                booking.setBookingStatus("PENDING_PAYMENT");
                paymentUrl = createVnpayPaymentUrl(finalTotalAmount, booking.getVnpTxnRef(), screening.getMovie().getNameVN(), httpServletRequest);
            } else {
                booking.setBookingStatus("PENDING_PAYMENT"); // Cho các ví khác nếu có tích hợp tương tự
            }
        } else {
            booking.setBookingStatus("UNKNOWN"); // Hoặc một trạng thái mặc định khác
        }

        Booking savedBooking = bookingRepository.save(booking);

        List<BookedSeat> bookedSeatEntities = new ArrayList<>();
        for (Seat seat : selectedSeats) {
            BookedSeat bookedSeat = new BookedSeat();
            bookedSeat.setBooking(savedBooking);
            bookedSeat.setSeat(seat);
            BigDecimal priceForThisSeat = baseTicketPrice;
            if (seat.getSeatType() != null && seat.getSeatType().getSeatTypePrice() != null) {
                priceForThisSeat = priceForThisSeat.add(seat.getSeatType().getSeatTypePrice());
            }
            // Phân bổ giảm giá nếu có (đơn giản là chia đều, hoặc có thể logic phức tạp hơn)
            if (selectedSeats.size() > 0 && discountAmount.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal discountPerSeat = discountAmount.divide(BigDecimal.valueOf(selectedSeats.size()), 2, BigDecimal.ROUND_HALF_UP);
                priceForThisSeat = priceForThisSeat.subtract(discountPerSeat);
                if (priceForThisSeat.compareTo(BigDecimal.ZERO) < 0) priceForThisSeat = BigDecimal.ZERO;
            }
            bookedSeat.setPricePaid(priceForThisSeat);
            bookedSeatEntities.add(bookedSeat);
        }
        bookedSeatRepository.saveAll(bookedSeatEntities);

        return convertToBookingDetailResponseDTO(savedBooking, paymentUrl, originalTotalAmount);
    }

    private String createVnpayPaymentUrl(BigDecimal amount, String vnpTxnRef, String orderInfoDesc, HttpServletRequest req) {
        long amountLong = amount.multiply(BigDecimal.valueOf(100)).longValue();

        Map<String, String> vnp_Params = new TreeMap<>(); // Sử dụng TreeMap để tự động sắp xếp theo key
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

        // Build hashData và queryUrl
        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();

        for (Map.Entry<String, String> entry : vnp_Params.entrySet()) {
            String fieldName = entry.getKey();
            String fieldValue = entry.getValue();
            if ((fieldValue != null) && (fieldValue.length() > 0)) {
                // Build hash data (value được URL encode)
                if (hashData.length() > 0) {
                    hashData.append('&');
                }
                hashData.append(fieldName);
                hashData.append('=');
                try {
                    hashData.append(URLEncoder.encode(fieldValue, StandardCharsets.UTF_8.toString()));
                } catch (UnsupportedEncodingException e) {
                    // Nên log lỗi và có thể throw exception thay vì chỉ printStackTrace
                    throw new RuntimeException("Error encoding VNPAY param: " + fieldName, e);
                }

                // Build query string (cả key và value đều được URL encode)
                if (query.length() > 0) {
                    query.append('&');
                }
                try {
                    query.append(URLEncoder.encode(fieldName, StandardCharsets.UTF_8.toString()));
                    query.append('=');
                    query.append(URLEncoder.encode(fieldValue, StandardCharsets.UTF_8.toString()));
                } catch (UnsupportedEncodingException e) {
                    throw new RuntimeException("Error encoding VNPAY query param: " + fieldName, e);
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
    public String handleVnpayReturn(Map<String, String> vnpayParams) {
        logger.info("Handling VNPAY return with params: {}", vnpayParams);

        String vnp_SecureHash_FromVNPay = vnpayParams.remove("vnp_SecureHash"); // QUAN TRỌNG: remove() sẽ thay đổi map vnpayParams
        if (vnp_SecureHash_FromVNPay == null) {
            logger.error("VNPAY return missing vnp_SecureHash. Params: {}", vnpayParams);
            // Không có bookingId ở đây vì không thể xác thực, có thể redirect về trang lỗi chung của frontend
            return vnpayConfig.getFrontendFailureUrl() + "?reason=missing_secure_hash";
        }
        logger.info("SecureHash from VNPAY: {}", vnp_SecureHash_FromVNPay);

        // Tạo lại hash từ các params còn lại (sau khi đã remove vnp_SecureHash)
        String calculatedSecureHash = VnpayConfig.hashAllFields(vnpayParams, vnpayConfig.getVnp_HashSecret());
        logger.info("Calculated SecureHash by backend: {}", calculatedSecureHash);

        String vnp_TxnRef = vnpayParams.get("vnp_TxnRef");
        String vnp_ResponseCode = vnpayParams.get("vnp_ResponseCode");
        // String vnp_TransactionStatus = vnpayParams.get("vnp_TransactionStatus"); // Có thể dùng nếu cần kiểm tra thêm

        if (vnp_TxnRef == null || vnp_TxnRef.isEmpty()) {
            logger.error("VNPAY return missing vnp_TxnRef. Params: {}", vnpayParams);
            return vnpayConfig.getFrontendFailureUrl() + "?reason=missing_transaction_ref";
        }

        Optional<Booking> bookingOptional = bookingRepository.findByVnpTxnRef(vnp_TxnRef);

        if (!bookingOptional.isPresent()) {
            logger.warn("Booking not found for vnp_TxnRef: {}. Params: {}", vnp_TxnRef, vnpayParams);
            return vnpayConfig.getFrontendFailureUrl() + "?reason=booking_not_found&txnRef=" + vnp_TxnRef;
        }

        Booking booking = bookingOptional.get();
        logger.info("Found booking with ID: {} and Status: {} for vnp_TxnRef: {}", booking.getId(), booking.getBookingStatus(), vnp_TxnRef);

        // Lấy movieId để có thể trả về cho frontend nếu cần
        String movieIdParam = "";
        if (booking.getScreening() != null && booking.getScreening().getMovie() != null) {
            movieIdParam = "&movieId=" + booking.getScreening().getMovie().getId();
        }

        // Chỉ xử lý nếu booking đang ở trạng thái PENDING_PAYMENT
        if (!"PENDING_PAYMENT".equals(booking.getBookingStatus())) {
            logger.warn("Booking ID: {} is not in PENDING_PAYMENT state. Current status: {}. VNPAY ResponseCode: {}", booking.getId(), booking.getBookingStatus(), vnp_ResponseCode);
            if ("PAID".equals(booking.getBookingStatus())) {
                // Nếu đã PAID, có thể là user refresh trang hoặc VNPAY gọi lại
                return vnpayConfig.getFrontendSuccessUrl() + "?bookingId=" + booking.getId() + movieIdParam;
            }
            // Nếu là trạng thái khác (ví dụ: CANCELLED, FAILED trước đó), vẫn nên redirect về trang failure
            return vnpayConfig.getFrontendFailureUrl() + "?reason=booking_already_processed&bookingId=" + booking.getId() + movieIdParam;
        }

        // Xác thực chữ ký
        if (calculatedSecureHash.equals(vnp_SecureHash_FromVNPay)) {
            logger.info("SecureHash Matched for Booking ID: {}", booking.getId());
            if ("00".equals(vnp_ResponseCode)) { // Giao dịch thành công từ VNPAY
                logger.info("VNPAY transaction successful (ResponseCode 00) for Booking ID: {}. Updating status to PAID.", booking.getId());
                booking.setBookingStatus("PAID");
                // Có thể lưu thêm vnp_BankTranNo, vnp_TransactionNo nếu cần
                // booking.setVnpBankTranNo(vnpayParams.get("vnp_BankTranNo"));
                // booking.setVnpTransactionNo(vnpayParams.get("vnp_TransactionNo"));
                bookingRepository.save(booking);
                // TODO: Gửi email xác nhận, thông báo, etc.
                logger.info("Redirecting to success URL: {}", vnpayConfig.getFrontendSuccessUrl() + "?bookingId=" + booking.getId() + movieIdParam);
                return vnpayConfig.getFrontendSuccessUrl() + "?bookingId=" + booking.getId() + movieIdParam;
            } else { // Giao dịch thất bại từ VNPAY
                logger.warn("VNPAY transaction failed (ResponseCode: {}) for Booking ID: {}. Updating status to PAYMENT_FAILED.", vnp_ResponseCode, booking.getId());
                booking.setBookingStatus("PAYMENT_FAILED");
                bookingRepository.save(booking);
                logger.info("Redirecting to failure URL: {}", vnpayConfig.getFrontendFailureUrl() + "?reason=payment_declined&bookingId=" + booking.getId() + "&vnp_ResponseCode=" + vnp_ResponseCode + movieIdParam);
                return vnpayConfig.getFrontendFailureUrl() + "?reason=payment_declined&bookingId=" + booking.getId() + "&vnp_ResponseCode=" + vnp_ResponseCode + movieIdParam;
            }
        } else { // Sai chữ ký
            logger.error("SecureHash Mismatch for Booking ID: {}. Expected: [{}], Calculated: [{}]. Params: {}",
                    booking.getId(), vnp_SecureHash_FromVNPay, calculatedSecureHash, vnpayParams); // Log các params gốc trước khi remove hash
            booking.setBookingStatus("PAYMENT_FAILED"); // Coi như thất bại nếu chữ ký sai
            bookingRepository.save(booking);
            logger.info("Redirecting to failure URL due to invalid signature: {}", vnpayConfig.getFrontendFailureUrl() + "?reason=invalid_signature&bookingId=" + booking.getId() + movieIdParam);
            return vnpayConfig.getFrontendFailureUrl() + "?reason=invalid_signature&bookingId=" + booking.getId() + movieIdParam;
        }
    }
    public List<BookingDetailResponseDTO> getUserBookingHistory(Long accountId) {
        List<Booking> bookings = bookingRepository.findByAccountAccountIdOrderByBookingTimeDesc(accountId);
        return bookings.stream()
                .map(b -> convertToBookingDetailResponseDTO(b, null, calculateOriginalAmount(b)))
                .collect(Collectors.toList());
    }

    public BookingDetailResponseDTO getBookingDetailsForUser(Integer bookingId, Long accountId) {
        Booking booking = bookingRepository.findByIdAndAccountAccountId(bookingId, accountId)
                .orElseThrow(() -> new RuntimeException("Booking not found or does not belong to the user."));
        return convertToBookingDetailResponseDTO(booking, null, calculateOriginalAmount(booking));
    }


    private BigDecimal calculateOriginalAmount(Booking booking) {
        // Recalculate original amount if not stored, or fetch if stored
        // This is a simplified version. In a real scenario, you might store original_amount
        // or re-calculate more accurately based on FareType and SeatType prices at booking time.
        BigDecimal originalAmount = booking.getTotalAmount();
        if (booking.getDiscountApplied() != null) {
            originalAmount = originalAmount.add(booking.getDiscountApplied());
        }
        return originalAmount;
    }


    private BookingDetailResponseDTO convertToBookingDetailResponseDTO(Booking booking, String paymentUrl, BigDecimal originalAmount) {
        if (booking == null) return null;

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
                    // .detail(p.getDetail()) // Bạn chưa có detail trong Promotion model, nếu có thì thêm vào
                    .build();
        }

        BookingDetailResponseDTO.PaymentMethodInfoDTO paymentMethodInfo = null;
        if(booking.getPaymentMethod() != null){
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
                .account(accountInfo)
                .screening(screeningInfo)
                .promotion(promotionInfo)
                .paymentMethod(paymentMethodInfo)
                .promotionCodeApplied(booking.getPromotionCodeApplied())
                .discountTypeApplied(booking.getDiscountTypeApplied())
                .discountApplied(booking.getDiscountApplied())
                .bookingTime(booking.getBookingTime())
                .totalAmount(booking.getTotalAmount())
                .originalAmount(originalAmount) // Truyền vào
                .bookingStatus(booking.getBookingStatus())
                .bookedSeats(bookedSeatInfoList)
                .paymentUrl(paymentUrl) // Thêm paymentUrl vào response
                .build();
    }
}
