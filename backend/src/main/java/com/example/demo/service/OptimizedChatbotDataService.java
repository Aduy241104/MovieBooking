package com.example.demo.service;

import com.example.demo.model.*;
import com.example.demo.repository.*;
import com.example.demo.utils.QuestionAnalyzer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.apache.commons.text.similarity.JaroWinklerSimilarity;

import java.math.BigDecimal;
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
        return buildContextForIntent(info, "");
    }

    /**
     * Xây dựng context tối ưu dựa trên intent của user với câu hỏi gốc
     */
    public String buildContextForIntent(QuestionAnalyzer.QueryInfo info, String originalQuestion) {
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
                    return "Hiện tại chưa có thông tin ngày khởi chiếu cho phim này trong hệ thống.";
                }
            } else {
                return "Hiện tại chưa có phim '" + info.movieName + "' trong hệ thống.";
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
                    context.append(" - ").append(m.getNameVN()).append(" (").append(m.getNameEN()).append(")\n");
                }
            } else {
                return "Hiện tại chưa có phim nào thuộc thể loại " + info.genre + " trong hệ thống.";
            }
        }

        // 2. Phim đang chiếu
        if (info.movie_now_showing) {
            List<Movie> nowShowing = chatbotMovieRepository.findNowShowingMovies(LocalDate.now());
            if (!nowShowing.isEmpty()) {
                int count = 1;
                context.append("PHIM ĐANG CHIẾU:\n");
                for (Movie movie : nowShowing) {
                    context.append(count).append(". ").append(movie.getNameVN()).append(" (").append(movie.getNameEN())
                            .append(")\n");
                    count++;
                }
                // Nếu user chỉ hỏi phim đang chiếu mà không hỏi thông tin chi tiết
                if (!info.movie_info) {
                    context.append("\nBạn muốn xem thông tin chi tiết, lịch chiếu hay đặt vé cho phim nào không?");
                }
                context.append("\n");
            }
        }

        // 3. Phim sắp chiếu
        if (info.movie_coming_soon) {
            List<Movie> comingSoon = chatbotMovieRepository.findComingSoonMovies(LocalDate.now());
            if (!comingSoon.isEmpty()) {
                context.append("PHIM SẮP CHIẾU:\n");
                for (Movie movie : comingSoon) {
                    context.append(movie.getNameVN()).append(" (").append(movie.getNameEN())
                            .append(") Khởi chiếu: ").append(movie.getFromDate()).append("\n");
                }
            } else {
                return "Hiện tại chưa có phim nào sắp chiếu.";
            }
        }

        // 4. Bảng giá vé
        if (info.movie_pricing) {
            List<FareType> fareTypes = fareTypeRepository.findByIsDeletedFalse();
            if (!fareTypes.isEmpty()) {
                context.append("BẢNG GIÁ VÉ:\n");
                for (FareType fare : fareTypes) {
                    context.append("").append(fare.getName()).append(":\n");
                    context.append(" Giá cơ bản: ").append(String.format("%,d", fare.getBasePrice().intValue()))
                            .append(" VND\n");
                    context.append(" Giá theo ngày: ").append(String.format("%,d", fare.getDayPrice().intValue()))
                            .append(" VND\n");
                    if (fare.getTimeSlotType() != null) {
                        context.append(" Khung giờ: ").append(fare.getTimeSlotType()).append("\n");
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
                    BigDecimal discount = promo.getDiscountLevel();
                    int discountInt = discount.intValue();
                    context.append(promo.getCode());
                    context.append(" - Giảm ").append(discountInt);
                    if (discountInt > 99) {
                        context.append("%");
                    } else {
                        context.append(" VND");
                    }
                    if (promo.getMinOrder() != null) {
                        context.append(" (Đơn tối thiểu: ").append(String.format("%,d", promo.getMinOrder().intValue()))
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
            String showtimesContext = handleShowTimesQuery(info, new StringBuilder());
            if (!showtimesContext.isEmpty())
                context.append(showtimesContext).append("\n\n");
        }

        // 7.5. Chọn ghế và giá
        if (info.seat_selection) {
            String seatContext = handleSeatSelectionQuery(context);
            if (!seatContext.isEmpty()) {
                return seatContext;
            }
        }

        // 8. Thông tin rạp chiếu
        if (info.cinema_info && !info.seat_selection && !info.cinema_location) {
            return handleCinemaInfoQuery(context);
        }

        // 12. Hướng dẫn đặt vé
        if (info.movie_booking) {
            // Nếu user đã cung cấp tên phim, cung cấp hướng dẫn chi tiết + lịch chiếu
            if (info.movieName != null) {
                context.append("HƯỚNG DẪN ĐẶT VÉ CHO PHIM '").append(info.movieName.toUpperCase()).append("':\n\n");
                context.append("**Bước 1: Chọn suất chiếu phù hợp**\n");
                String showtimesContext = handleShowTimesQuery(info, new StringBuilder());
                if (!showtimesContext.isEmpty()) {
                    context.append(showtimesContext).append("\n");
                    context.append("**Bước 2: Các bước đặt vé**\n");
                    context.append("Chọn suất chiếu mong muốn từ danh sách trên\n");
                    context.append("Chọn loại ghế (thường/VIP) và vị trí ngồi\n");
                    context.append("Xác nhận thông tin và thanh toán\n");
                    context.append("Nhận vé điện tử qua email hoặc SMS\n\n");
                    context.append("**Lưu ý:** Sau khi đặt vé thành công, không thể đổi suất chiếu hoặc hủy vé.\n");
                } else {
                    context.append("Rất tiếc, hiện tại không có lịch chiếu cho phim '").append(info.movieName)
                            .append("'. Bạn có muốn tham khảo phim khác không?");
                }
            } else {
                // Nếu chưa có tên phim, hướng dẫn chung
                context.append("HƯỚNG DẪN ĐẶT VÉ TRỰC TUYẾN:\n");
                context.append("**Bước 1: Chọn phim**\n");
                context.append("Xem danh sách phim đang chiếu\n");
                context.append("Chọn phim bạn muốn xem\n\n");
                context.append("**Bước 2: Xem lịch chiếu**\n");
                context.append("Kiểm tra lịch chiếu của phim\n");
                context.append("Chọn ngày và giờ phù hợp\n\n");
                context.append("**Bước 3: Chọn ghế**\n");
                context.append("Chọn loại ghế (thường/VIP)\n");
                context.append("Chọn vị trí ngồi mong muốn\n\n");
                context.append("**Bước 4: Thanh toán**\n");
                context.append("Điền thông tin cá nhân\n");
                context.append("Chọn phương thức thanh toán\n");
                context.append("Hoàn tất đặt vé và nhận mã vé\n\n");
                context.append("Bạn muốn đặt vé cho phim nào? Hãy cho tôi biết tên phim nhé!\n");
            }
        }

        // 9. Thông tin phim cụ thể hoặc movie_info
        if (info.movie_info && info.movieName != null) {
            // User hỏi thông tin về một phim cụ thể
            String movieInfoContext = handleSpecificMovieQuery(info, new StringBuilder());
            if (!movieInfoContext.isEmpty()) {
                context.append(movieInfoContext).append("\n\n");
            }
        } else if (info.movie_info && info.movieName == null) {
            // User hỏi "thông tin phim" chung chung
            if (info.movie_now_showing) {
                // Nếu đồng thời hỏi "phim đang chiếu", thì không cần hỏi lại tên phim
                // mà bổ sung thông tin chi tiết cho các phim đã liệt kê ở mục 2.
                List<Movie> nowShowing = chatbotMovieRepository.findNowShowingMovies(LocalDate.now());
                if (!nowShowing.isEmpty()) {
                    context.append("CHI TIẾT CÁC PHIM ĐANG CHIẾU:\n");
                    for (Movie movie : nowShowing) {
                        // Tạo một QueryInfo tạm để lấy thông tin chi tiết cho từng phim
                        QuestionAnalyzer.QueryInfo tempInfo = new QuestionAnalyzer.QueryInfo();
                        tempInfo.movieName = movie.getNameVN();
                        context.append(handleSpecificMovieQuery(tempInfo, new StringBuilder()));
                        context.append("\n");
                    }
                }
            } else {
                // Nếu chỉ hỏi "thông tin phim" mà không nói rõ phim nào
                context.append(
                        "Bạn vui lòng cung cấp tên phim để xem thông tin chi tiết nhé (ví dụ: 'thông tin phim The Batman').\n");
            }
        }

        // 9b. Thông tin phim cụ thể (giữ lại cho các intent khác)
        if (info.movieName != null && !info.movie_showtimes && !info.movie_info) {
            return handleSpecificMovieQuery(info, context);
        }

        // 10. Địa chỉ rạp
        if (info.cinema_location) {
            return "ĐỊA CHỈ RẠP:\nMovieTheater Cinema\nĐịa chỉ: 600 Nguyễn Văn Cừ (nối dài), P. An Bình, Q. Ninh Kiều, TP. Cần Thơ\n";
        }

        // 11. Liên hệ hỗ trợ
        if (info.contact_support) {
            return "LIÊN HỆ:\nHotline: 1900-6069\nEmail: support@movietheater.com\nWebsite: www.movietheater.com";
        }

        // 13. Quản lý tài khoản
        if (info.account_management) {
            context.append("HƯỚNG DẪN QUẢN LÝ TÀI KHOẢN:\n");
            String lowerQuestion = originalQuestion.toLowerCase();

            if (lowerQuestion.contains("mật khẩu") || lowerQuestion.contains("password") ||
                    lowerQuestion.contains("quên") || lowerQuestion.contains("forgot") ||
                    lowerQuestion.contains("lấy lại") || lowerQuestion.contains("reset")) {
                context.append("**Lấy lại mật khẩu:**\n");
                context.append("Truy cập trang đăng nhập\n");
                context.append("Nhấp vào 'Quên mật khẩu?'\n");
                context.append("Nhập email đã đăng ký\n");
                context.append("Kiểm tra email và làm theo hướng dẫn\n");
            } else if (lowerQuestion.contains("đăng ký") || lowerQuestion.contains("đăng kí") ||
                    lowerQuestion.contains("register") || lowerQuestion.contains("tạo tài khoản") ||
                    lowerQuestion.contains("sign up")) {
                context.append("**Đăng ký tài khoản mới:**\n");
                context.append("Truy cập trang đăng ký\n");
                context.append("Điền thông tin cá nhân (họ tên, email, số điện thoại)\n");
                context.append("Tạo mật khẩu mạnh\n");
                context.append("Xác nhận email để kích hoạt tài khoản\n");
            } else if (lowerQuestion.contains("cập nhật") || lowerQuestion.contains("chỉnh sửa") ||
                    lowerQuestion.contains("thay đổi") || lowerQuestion.contains("sửa") ||
                    lowerQuestion.contains("update") || lowerQuestion.contains("edit")) {
                context.append("**Cập nhật thông tin tài khoản:**\n");
                context.append("Đăng nhập vào tài khoản\n");
                context.append("Vào mục 'Thông tin cá nhân'\n");
                context.append("Chỉnh sửa thông tin cần thiết\n");
                context.append("Nhấn 'Lưu thay đổi' để hoàn tất\n");
            } else {
                context.append("**Các chức năng quản lý tài khoản:**\n");
                context.append("Đăng ký tài khoản mới\n");
                context.append("Lấy lại mật khẩu\n");
                context.append("Cập nhật thông tin cá nhân\n");
                context.append("Xem lịch sử đặt vé\n");
                context.append("\nVui lòng cung cấp thêm thông tin về việc bạn cần hỗ trợ.\n");
            }
        }

        // Nếu không có intent nào được xác định rõ ràng nhưng có tên phim
        if (context.isEmpty() && info.movieName != null) {
            return handleSpecificMovieQuery(info, context);
        }

        // Nếu context vẫn rỗng, trả về thông báo chuẩn để tránh hallucination
        if (context.toString().trim().isEmpty() && info.hasAnyIntent()) {
            return "Hiện tại chưa có thông tin này trong hệ thống. Tôi chỉ có thể hỗ trợ về: phim đang chiếu, lịch chiếu, đặt vé, bảng giá, khuyến mãi, địa chỉ rạp và quản lý tài khoản.";
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
                        context.append("").append(screening.getShowDateTime().toLocalDate().format(dateFormatter))
                                .append(" - ").append(screening.getShowDateTime().toLocalTime())
                                .append(" (Phòng ").append(screening.getCinemaRoom().getCinemaRoomName()).append(")\n");
                    }
                } else {
                    return "Hiện tại chưa có lịch chiếu cho phim '" + movie.getNameVN() + "'.";
                }
            } else {
                return "Hiện tại chưa có phim '" + info.movieName + "' trong hệ thống.";
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
                    context.append("").append(screening.getMovie().getNameVN())
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

    private String handleSeatSelectionQuery(StringBuilder context) {
        // Lấy thông tin rạp và ghế
        List<CinemaRoom> rooms = chatbotCinemaRepository.findAllByIsDeletedFalse();
        List<FareType> fareTypes = fareTypeRepository.findByIsDeletedFalse();

        context.append("THÔNG TIN LOẠI GHẾ VÀ GIÁ VÉ:\n\n");

        // Mapping loại ghế với từng phòng
        context.append("**CÁC LOẠI PHÒNG VÀ LOẠI GHẾ:**\n");
        for (CinemaRoom room : rooms) {
            String roomNameLower = room.getCinemaRoomName().toLowerCase();
            String type;
            if (roomNameLower.contains("vip"))
                type = "Ghế VIP";
            else if (roomNameLower.contains("đôi"))
                type = "Ghế đôi";
            else
                type = "Ghế thường";
            final String fareType = type;
            FareType fareMatch = null;
            for (FareType fare : fareTypes) {
                if (fare.getName().equalsIgnoreCase(fareType)) {
                    fareMatch = fare;
                    break;
                }
            }
            context.append("• ").append(room.getCinemaRoomName())
                    .append(" - ").append(room.getSeatQuantity()).append(" ghế (Loại: ").append(type).append(")");
            if (fareMatch != null) {
                context.append(" - Giá cơ bản: ").append(String.format("%,d", fareMatch.getBasePrice().intValue()))
                        .append(" VND");
            }
            context.append("\n");
        }
        context.append("\n");

        // Thông tin về loại ghế từ fareTypes
        if (!fareTypes.isEmpty()) {
            context.append("**BẢNG GIÁ THEO LOẠI GHẾ:**\n");
            for (FareType fare : fareTypes) {
                context.append("• ").append(fare.getName()).append(": ");
                context.append("Giá cơ bản: ").append(String.format("%,d", fare.getBasePrice().intValue()))
                        .append(" VND, ");
                context.append("Giá theo ngày: ").append(String.format("%,d", fare.getDayPrice().intValue()))
                        .append(" VND");
                if (fare.getTimeSlotType() != null) {
                    context.append(", Khung giờ: ").append(fare.getTimeSlotType());
                }
                context.append("\n");
            }
        }

        // Hướng dẫn chọn ghế
        context.append("**HƯỚNG DẪN CHỌN GHẾ:**\n");
        context.append("1. Ghế thường: Giá cơ bản, vị trí tiêu chuẩn\n");
        context.append("2. Ghế VIP: Ghế da cao cấp, vị trí tốt nhất\n");
        context.append("3. Ghế đôi: Dành cho cặp đôi, không có tay chia giữa\n\n");
        context.append("**Lưu ý:** Giá vé có thể thay đổi theo ngày trong tuần và khung giờ chiếu.\n");

        return context.toString();
    }

    private String handleSpecificMovieQuery(QuestionAnalyzer.QueryInfo info, StringBuilder context) {
        List<Movie> foundMovies = findMoviesByName(info.movieName);
        if (!foundMovies.isEmpty()) {
            Movie movie = foundMovies.getFirst();
            context.append("THÔNG TIN PHIM **").append(movie.getNameVN()).append("**:\n");
            context.append("Tên gốc: ").append(movie.getNameEN()).append("\n");
            context.append("Thời lượng: ").append(movie.getDuration()).append(" phút\n");
            context.append("Đạo diễn: ").append(movie.getDirector()).append("\n");
            context.append("Giới hạn tuổi: ").append(movie.getAgeLimit()).append("\n");

            if (movie.getContent() != null && !movie.getContent().isEmpty()) {
                String content = movie.getContent();
                if (content.length() > 200) {
                    content = content.substring(0, 200) + "...";
                }
                context.append("Nội dung: ").append(content).append("\n");
            }

            // Thêm vài suất chiếu gần nhất
            List<Screening> screenings = chatbotScreeningRepository.findByMovieName(info.movieName,
                    LocalDateTime.now());
            if (!screenings.isEmpty()) {
                context.append("Suất chiếu gần nhất:\n");
                for (int i = 0; i < Math.min(3, screenings.size()); i++) {
                    Screening screening = screenings.get(i);
                    context.append(screening.getShowDateTime().toLocalDate())
                            .append(" ").append(screening.getShowDateTime().toLocalTime())
                            .append(" (").append(screening.getCinemaRoom().getCinemaRoomName()).append(")\n");
                }
            }
        } else {
            return "Hiện tại chưa có thông tin về phim này trong hệ thống.";
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

        // Chuẩn hóa input: bỏ dấu, lowercase, loại bỏ khoảng trắng thừa, ký tự đặc biệt
        String normalizedInput = normalizeMovieName(movieName);
        List<Movie> allMovies = chatbotMovieRepository.findAll();

        // Tìm kiếm fuzzy: so sánh với cả tên tiếng Việt và tiếng Anh đã chuẩn hóa
        // Sử dụng JaroWinklerSimilarity cho fuzzy matching
        JaroWinklerSimilarity similarity = new JaroWinklerSimilarity();

        return allMovies.stream()
                .map(movie -> {
                    String vnName = normalizeMovieName(movie.getNameVN());
                    String enName = normalizeMovieName(movie.getNameEN());
                    double vnScore = similarity.apply(normalizedInput, vnName);
                    double enScore = similarity.apply(normalizedInput, enName);
                    double maxScore = Math.max(vnScore, enScore);
                    return new java.util.AbstractMap.SimpleEntry<>(movie, maxScore);
                })
                .filter(entry -> entry.getValue() > 0.80) // Ngưỡng có thể điều chỉnh
                .sorted((e1, e2) -> Double.compare(e2.getValue(), e1.getValue()))
                .limit(5)
                .map(java.util.Map.Entry::getKey)
                .toList();
    }

    /**
     * Loại bỏ dấu tiếng Việt
     */
    private String removeAccents(String text) {
        if (text == null)
            return "";
        return text.replaceAll("[àáảãạăắằẳẵặâấầẨẪậ]", "a")
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

    /**
     * Chuẩn hóa tên phim: bỏ dấu, lowercase, loại bỏ khoảng trắng thừa, ký tự đặc
     * biệt phổ biến
     */
    private String normalizeMovieName(String name) {
        if (name == null)
            return "";
        // Bỏ dấu
        String s = removeAccents(name);
        // Lowercase
        s = s.toLowerCase();
        // Loại bỏ các ký tự đặc biệt phổ biến
        s = s.replaceAll("[:\\-_,.!?'\"]", "");
        // Loại bỏ khoảng trắng thừa
        s = s.replaceAll("\\s+", " ").trim();
        return s;
    }
}
