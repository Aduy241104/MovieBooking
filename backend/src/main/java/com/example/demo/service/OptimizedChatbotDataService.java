package com.example.demo.service;

import com.example.demo.model.*;
import com.example.demo.repository.*;
import com.example.demo.utils.QuestionAnalyzer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Collections;

/**
 * Service tối ưu cho việc xây dựng context chatbot
 * Chỉ lấy dữ liệu thực sự cần thiết dựa trên intent
 */
@Service
public class OptimizedChatbotDataService {

    @Autowired
    private ChatbotMovieRepository chatbotMovieRepository;

    @Autowired
    private ChatbotScreeningRepository chatbotScreeningRepository;

    @Autowired
    private ChatbotCinemaRepository chatbotCinemaRepository;

    @Autowired
    private PaymentMethodRepository paymentMethodRepository;

    @Autowired
    private FareTypeRepository fareTypeRepository;

    @Autowired
    private PromotionRepository promotionRepository;

    @Autowired
    private MovieTypeRepository movieTypeRepository;

    // Định dạng ngày tháng sử dụng trong hệ thống
    DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    /**
     * Xây dựng context tối ưu dựa trên intent của user
     */
    public String buildContextForIntent(QuestionAnalyzer.QueryInfo info) {
        StringBuilder context = new StringBuilder();

        // Xử lý intent hỏi ngày khởi chiếu phim
        if (info.movie_release_date && info.movieName != null) {
            List<Movie> foundMovies = findMoviesByName(info.movieName);
            if (!foundMovies.isEmpty()) {
                Movie movie = foundMovies.get(0);
                if (movie.getFromDate() != null) {
                    return "Phim '" + movie.getNameVN() + "' (" + movie.getNameEN() + ") sẽ khởi chiếu từ ngày: "
                            + movie.getFromDate().format(dateFormatter);
                } else {
                    return "Xin lỗi, hiện tại hệ thống chưa có thông tin ngày khởi chiếu cho phim này.";
                }
            } else {
                return "Không tìm thấy phim '" + info.movieName + "' trong hệ thống.";
            }
        }
        // 1. Thể loại phim
        if (info.genre != null && info.movie_genre) {
            List<MovieType> movieTypes = movieTypeRepository
                    .findByType_NameIgnoreCaseAndMovie_IsDeletedFalse(info.genre);
            if (!movieTypes.isEmpty()) {
                context.append("PHIM THỂ LOẠI ").append(info.genre.toUpperCase()).append(":\n");
                for (MovieType mt : movieTypes) {
                    Movie m = mt.getMovie();
                    context.append("• ").append(m.getNameVN()).append(" (").append(m.getNameEN()).append(")\n");
                }
            } else {
                return "Hiện tại chưa có phim nào thuộc thể loại " + info.genre + " trong hệ thống.";
            }
        }

        // 2. Phim đang chiếu
        if (info.movie_now_showing) {
            List<Movie> nowShowing = chatbotMovieRepository.findNowShowingMovies(LocalDate.now());
            if (!nowShowing.isEmpty()) {
                context.append("PHIM ĐANG CHIẾU:\n");
                for (Movie movie : nowShowing) {
                    context.append("• ").append(movie.getNameVN()).append(" (").append(movie.getNameEN()).append(")\n");
                }
            } else {
                return "Hiện tại chưa có phim nào đang chiếu.";
            }
        }

        // 3. Phim sắp chiếu
        if (info.movie_coming_soon) {
            List<Movie> comingSoon = chatbotMovieRepository.findComingSoonMovies(LocalDate.now());
            if (!comingSoon.isEmpty()) {
                context.append("PHIM SẮP CHIẾU:\n");
                for (Movie movie : comingSoon) {
                    context.append("• ").append(movie.getNameVN()).append(" (").append(movie.getNameEN())
                            .append(") - Khởi chiếu: ").append(movie.getFromDate()).append("\n");
                }
            } else {
                return "Hiện tại chưa có phim nào sắp chiếu.";
            }
        }

        // 4. Bảng giá vé
        if (info.movie_pricing || info.promotion_discount) {
            List<FareType> fareTypes = fareTypeRepository.findByIsDeletedFalse();
            if (!fareTypes.isEmpty()) {
                context.append("BẢNG GIÁ VÉ:\n");
                for (FareType fare : fareTypes) {
                    context.append("• ").append(fare.getName()).append(":\n");
                    context.append("  - Giá cơ bản: ").append(String.format("%,d", fare.getBasePrice()))
                            .append(" VND\n");
                    context.append("  - Giá theo ngày: ").append(String.format("%,d", fare.getDayPrice()))
                            .append(" VND\n");
                    if (fare.getTimeSlotType() != null) {
                        context.append("  - Khung giờ: ").append(fare.getTimeSlotType()).append("\n");
                    }
                }
            } else {
                return "Hiện tại chưa có thông tin bảng giá.";
            }
        }

        // 5. Khuyến mãi
        if (info.promotion_discount) {
            List<Promotion> promotions = promotionRepository.findByActiveTrue();
            if (!promotions.isEmpty()) {
                context.append("KHUYẾN MÃI HIỆN TẠI:\n");
                for (Promotion promo : promotions) {
                    context.append("• ").append(promo.getCode());
                    context.append(" - Giảm ").append(promo.getDiscountLevel()).append("%");
                    if (promo.getMinOrder() != null) {
                        context.append(" (Đơn tối thiểu: ").append(String.format("%,d", promo.getMinOrder()))
                                .append(" VND)");
                    }
                    context.append("\n");
                }
            } else {
                return "Hiện tại chưa có khuyến mãi nào.";
            }
        }

        // 6. Phương thức thanh toán
        if (info.payment_methods) {
            List<PaymentMethod> payments = paymentMethodRepository.findByActiveTrue();
            if (!payments.isEmpty()) {
                context.append("PHƯƠNG THỨC THANH TOÁN:\n");
                for (PaymentMethod payment : payments) {
                    context.append("• ").append(payment.getName()).append("\n");
                }
            } else {
                return "Hiện tại chưa có thông tin phương thức thanh toán.";
            }
        }

        // 7. Lịch chiếu
        if (info.movie_showtimes) {
            return handleShowTimesQuery(info, context);
        }

        // 8. Thông tin rạp chiếu
        if (info.cinema_info) {
            return handleCinemaInfoQuery(context);
        }

        // 9. Thông tin phim cụ thể hoặc movie_info
        if (info.movie_info) {
            if (info.movieName != null) {
                return handleSpecificMovieQuery(info, context);
            } else {
                // Nếu không có tên phim, lấy context cũ (nếu có) hoặc hướng dẫn user
                return "Bạn vui lòng cung cấp tên phim để xem thông tin chi tiết (ví dụ: 'giới hạn tuổi phim Spider-Man: No Way Home').";
            }
        }

        // 9b. Thông tin phim cụ thể (giữ lại cho các intent khác)
        if (info.movieName != null && !info.movie_showtimes) {
            return handleSpecificMovieQuery(info, context);
        }

        // 10. Địa chỉ rạp
        if (info.cinema_location) {
            return "ĐỊA CHỈ RẠP:\n• MovieTheater Cinema\n• Địa chỉ: 600 Nguyễn Văn Cừ (nối dài), P. An Bình, Q. Ninh Kiều, TP. Cần Thơ\n";
        }

        // 11. Liên hệ hỗ trợ
        if (info.contact_support) {
            return "LIÊN HỆ:\n• Hotline: 1900-6069\n• Email: support@movietheater.com\n• Website: www.movietheater.com";
        }

        // 12. Giờ hoạt động
        if (info.operating_hours) {
            return "GIỜ HOẠT ĐỘNG:\n• Thứ 2 - Chủ nhật: 9:00 - 23:00\n• Suất chiếu cuối: 21:30\n• Bán vé online 24/7";
        }

        // 13. Chính sách vé
        if (info.ticket_policy) {
            return "QUY ĐỊNH ĐỔI/TRẢ VÉ:\nSau khi đặt vé thành công, khách hàng KHÔNG thể đổi suất chiếu hoặc hủy/hoàn vé. Tuy nhiên, bạn có thể chuyển vé cho người khác sử dụng.";
        }

        // 14. Quản lý tài khoản
        if (info.account_management) {
            return "TÀI KHOẢN:\n- Đăng ký/Quên mật khẩu/Cập nhật thông tin: Vui lòng truy cập website hoặc liên hệ hotline để được hỗ trợ nhanh nhất.";
        }

        // 15. Hỗ trợ kỹ thuật
        if (info.technical_support) {
            return "HỖ TRỢ KỸ THUẬT:\nNếu gặp lỗi hệ thống, vui lòng liên hệ hotline hoặc gửi email để được hỗ trợ.";
        }

        return context.toString();
    }

    private String handleShowTimesQuery(QuestionAnalyzer.QueryInfo info, StringBuilder context) {
        if (info.movieName != null) {
            // Lịch chiếu phim cụ thể
            List<Movie> foundMovies = findMoviesByName(info.movieName);
            if (!foundMovies.isEmpty()) {
                Movie movie = foundMovies.get(0);
                List<Screening> screenings = chatbotScreeningRepository.findScreeningsByMovieKeyword(
                        info.movieName, LocalDateTime.now());
                if (!screenings.isEmpty()) {
                    context.append("LỊCH CHIẾU '").append(movie.getNameVN()).append("' (")
                            .append(movie.getNameEN()).append("):\n");
                    for (int i = 0; i < Math.min(10, screenings.size()); i++) {
                        Screening screening = screenings.get(i);
                        context.append("• ").append(screening.getShowDateTime().toLocalDate().format(dateFormatter))
                                .append(" - ").append(screening.getShowDateTime().toLocalTime())
                                .append(" (Phòng ").append(screening.getCinemaRoom().getCinemaRoomName()).append(")\n");
                    }
                } else {
                    return "Hiện tại chưa có lịch chiếu cho phim '" + movie.getNameVN() + "'.";
                }
            } else {
                return "Không tìm thấy phim '" + info.movieName + "' trong hệ thống.";
            }
        } else if ("today".equals(info.date)) {
            // Lịch chiếu hôm nay
            List<Screening> todayScreenings = chatbotScreeningRepository.findTodayScreenings(LocalDateTime.now());
            if (!todayScreenings.isEmpty()) {
                context.append("LỊCH CHIẾU HÔM NAY:\n");
                for (int i = 0; i < Math.min(15, todayScreenings.size()); i++) {
                    Screening screening = todayScreenings.get(i);
                    context.append("• ").append(screening.getMovie().getNameVN())
                            .append(" - ").append(screening.getShowDateTime().toLocalTime())
                            .append(" (Phòng ").append(screening.getCinemaRoom().getCinemaRoomName()).append(")\n");
                }
            } else {
                return "Hôm nay chưa có lịch chiếu nào.";
            }
        } else if ("tomorrow".equals(info.date)) {
            // Lịch chiếu ngày mai
            List<Screening> tomorrowScreenings = chatbotScreeningRepository
                    .findTomorrowScreenings(LocalDate.now().plusDays(1));
            if (!tomorrowScreenings.isEmpty()) {
                context.append("LỊCH CHIẾU NGÀY MAI:\n");
                for (int i = 0; i < Math.min(15, tomorrowScreenings.size()); i++) {
                    Screening screening = tomorrowScreenings.get(i);
                    context.append("• ").append(screening.getMovie().getNameVN())
                            .append(" - ").append(screening.getShowDateTime().toLocalTime())
                            .append(" (Phòng ").append(screening.getCinemaRoom().getCinemaRoomName()).append(")\n");
                }
            } else {
                return "Ngày mai chưa có lịch chiếu nào.";
            }
        }
        return context.toString();
    }

    private String handleCinemaInfoQuery(StringBuilder context) {
        List<CinemaRoom> rooms = chatbotCinemaRepository.findAllByIsDeletedFalse();
        Integer totalSeats = chatbotCinemaRepository.getTotalSeatsCount();

        context.append("THÔNG TIN RẠP CHIẾU:\n");
        context.append("• Tổng số phòng: ").append(rooms.size()).append("\n");
        context.append("• Tổng số ghế: ").append(totalSeats != null ? totalSeats : 0).append("\n");

        List<CinemaRoom> vipRooms = chatbotCinemaRepository.findVipRooms();
        if (!vipRooms.isEmpty()) {
            context.append("• Phòng VIP: ");
            for (int i = 0; i < vipRooms.size(); i++) {
                if (i > 0)
                    context.append(", ");
                context.append(vipRooms.get(i).getCinemaRoomName());
            }
            context.append("\n");
        }
        return context.toString();
    }

    private String handleSpecificMovieQuery(QuestionAnalyzer.QueryInfo info, StringBuilder context) {
        List<Movie> foundMovies = findMoviesByName(info.movieName);
        if (!foundMovies.isEmpty()) {
            Movie movie = foundMovies.get(0);
            context.append("THÔNG TIN PHIM '").append(movie.getNameVN()).append("':\n");
            context.append("• Tên gốc: ").append(movie.getNameEN()).append("\n");
            context.append("• Thời lượng: ").append(movie.getDuration()).append(" phút\n");
            context.append("• Đạo diễn: ").append(movie.getDirector()).append("\n");
            context.append("• Giới hạn tuổi: ").append(movie.getAgeLimit()).append("\n");

            if (movie.getContent() != null && !movie.getContent().isEmpty()) {
                String content = movie.getContent();
                if (content.length() > 200) {
                    content = content.substring(0, 200) + "...";
                }
                context.append("• Nội dung: ").append(content).append("\n");
            }

            // Thêm vài suất chiếu gần nhất
            List<Screening> screenings = chatbotScreeningRepository.findByMovieName(info.movieName,
                    LocalDateTime.now());
            if (!screenings.isEmpty()) {
                context.append("• Suất chiếu gần nhất:\n");
                for (int i = 0; i < Math.min(3, screenings.size()); i++) {
                    Screening screening = screenings.get(i);
                    context.append("  - ").append(screening.getShowDateTime().toLocalDate())
                            .append(" ").append(screening.getShowDateTime().toLocalTime())
                            .append(" (Phòng ").append(screening.getCinemaRoom().getCinemaRoomName()).append(")\n");
                }
            }
        } else {
            return "Xin lỗi, hiện tại hệ thống chưa có thông tin về phim này.";
        }
        return context.toString();
    }

    /**
     * Tìm phim theo tên với hỗ trợ tìm kiếm không dấu và fuzzy matching
     */
    private List<Movie> findMoviesByName(String movieName) {
        if (movieName == null || movieName.trim().isEmpty()) {
            return Collections.emptyList();
        }

        // Thử tìm chính xác trước
        List<Movie> foundMovies = chatbotMovieRepository.findByNameContaining(movieName);
        if (!foundMovies.isEmpty()) {
            return foundMovies;
        }

        // Tìm kiếm không dấu
        String normalizedInput = removeAccents(movieName.toLowerCase());
        List<Movie> allMovies = chatbotMovieRepository.findAll();

        return allMovies.stream()
                .filter(movie -> {
                    String vnName = removeAccents(movie.getNameVN().toLowerCase());
                    String enName = removeAccents(movie.getNameEN().toLowerCase());
                    return vnName.contains(normalizedInput) || enName.contains(normalizedInput);
                })
                .limit(5)
                .toList();
    }

    /**
     * Loại bỏ dấu tiếng Việt
     */
    private String removeAccents(String text) {
        if (text == null)
            return "";
        return text.replaceAll("[àáảãạăắằẳẵặâấầẩẫậ]", "a")
                .replaceAll("[èéẻẽẹêếềểễệ]", "e")
                .replaceAll("[ìíỉĩị]", "i")
                .replaceAll("[òóỏõọôốồổỗộơớờởỡợ]", "o")
                .replaceAll("[ùúủũụưứừửữự]", "u")
                .replaceAll("[ỳýỷỹỵ]", "y")
                .replaceAll("[đ]", "d")
                .replaceAll("[ÀÁẢÃẠĂẮẰẲẴẶÂẤẦẨẪẬ]", "A")
                .replaceAll("[ÈÉẺẼẸÊẾỀỂỄỆ]", "E")
                .replaceAll("[ÌÍỈĨỊ]", "I")
                .replaceAll("[ÒÓỎÕỌÔỐỒỔỖỘƠỚỜỞỠỢ]", "O")
                .replaceAll("[ÙÚỦŨỤƯỨỪỬỮỰ]", "U")
                .replaceAll("[ỲÝỶỸỴ]", "Y")
                .replaceAll("[Đ]", "D");
    }
}
