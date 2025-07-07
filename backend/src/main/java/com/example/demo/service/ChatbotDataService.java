package com.example.demo.service;

import com.example.demo.model.*;
import com.example.demo.repository.*;
import com.example.demo.utils.QuestionAnalyzer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Predicate;

@Service
public class ChatbotDataService {

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

    // Hàm tiện ích: kiểm tra chỉ có đúng 1 intent cụ thể được bật
    private boolean isOnlyIntent(QuestionAnalyzer.QueryInfo info,
            Predicate<QuestionAnalyzer.QueryInfo> intentCheck) {
        // Đếm số intent true (ngoại trừ các trường dữ liệu như genre, movieName...)
        int count = 0;
        if (info.genre != null)
            count++;
        if (info.askPromotion)
            count++;
        if (info.askDirector)
            count++;
        if (info.askActor)
            count++;
        if (info.askDuration)
            count++;
        if (info.askPrice)
            count++;
        if (info.askShowTimes)
            count++;
        if (info.askLanguage)
            count++;
        if (info.askAgeLimit)
            count++;
        if (info.askTrailer)
            count++;
        if (info.askBooking)
            count++;
        if (info.askReview)
            count++;
        if (info.askCinemaInfo)
            count++;
        if (info.askPayment)
            count++;
        if (info.askSeatSelection)
            count++;
        if (info.askGroupDiscount)
            count++;
        if (info.askStudentDiscount)
            count++;
        if (info.askLocation)
            count++;
        if (info.askContact)
            count++;
        if (info.askOpenHours)
            count++;
        if (info.askComingSoon)
            count++;
        if (info.askNowShowing)
            count++;
        if (info.askTop)
            count++;
        if (info.askNew)
            count++;
        if (info.askSubtitle)
            count++;
        if (info.askDubbing)
            count++;
        if (info.ask3D)
            count++;
        if (info.askError)
            count++;
        if (info.askHelp)
            count++;
        if (info.askChangeTicket)
            count++;
        if (info.askRefund)
            count++;
        if (info.askForgotPassword)
            count++;
        if (info.askRegister)
            count++;
        if (info.askUpdateProfile)
            count++;
        // ... có thể bổ sung intent mới ở đây
        // Chỉ đúng 1 intent được bật và intent đó là intentCheck
        return count == 1 && intentCheck.test(info);
    }

    public String buildContextForIntent(QuestionAnalyzer.QueryInfo info) {
        StringBuilder context = new StringBuilder();

        // Thêm thông tin về thể loại phim nếu có
        if (info.genre != null) {
            List<MovieType> movieTypes = movieTypeRepository
                    .findByType_NameIgnoreCaseAndMovie_IsDeletedFalse(info.genre);
            if (!movieTypes.isEmpty()) {
                context.append("Danh sách phim thể loại ").append(info.genre).append(":\n");
                int i = 1;
                for (MovieType mt : movieTypes) {
                    Movie m = mt.getMovie();
                    context.append(i++).append(". ").append(m.getNameVN()).append(" (").append(m.getNameEN())
                            .append(")\n");
                }
            } else {
                context.append("Không có phim nào thuộc thể loại ").append(info.genre).append(".\n");
            }
        }

        // Xử lý các intent cụ thể
        // ===== //
        // Hiển thị danh sách phim đang chiếu
        if (info.askNowShowing) {
            List<Movie> nowShowing = chatbotMovieRepository.findNowShowingMovies(LocalDate.now());
            if (!nowShowing.isEmpty()) {
                context.append("PHIM ĐANG CHIẾU:\n");
                for (Movie movie : nowShowing) {
                    context.append("- ").append(movie.getNameVN()).append(" (").append(movie.getNameEN()).append(")\n");
                }
            } else {
                context.append("Hiện tại rạp chưa có phim nào đang chiếu.\n");
            }
        }

        // Hiển thị danh sách phim sắp chiếu
        if (info.askComingSoon) {
            List<Movie> comingSoon = chatbotMovieRepository.findComingSoonMovies(LocalDate.now());
            if (!comingSoon.isEmpty()) {
                context.append("PHIM SẮP CHIẾU:\n");
                for (Movie movie : comingSoon) {
                    context.append("- ").append(movie.getNameVN()).append(" (").append(movie.getNameEN())
                            .append(") - Khởi chiếu: ").append(movie.getFromDate()).append("\n");
                }
            } else {
                context.append("Hiện tại rạp chưa có phim nào sắp chiếu.\n");
            }
        }

        // Hiển thị top phim hot/bom tấn
        if (info.askTop) {
            // Lấy phim hot theo số vé bán ra trong tuần hiện tại
            LocalDateTime now = LocalDateTime.now();
            // Tìm ngày đầu tuần (thứ 2)
            LocalDateTime startOfWeek = now.with(java.time.DayOfWeek.MONDAY).toLocalDate().atStartOfDay();
            LocalDateTime endOfWeek = startOfWeek.plusDays(7);
            List<Movie> topMovies = chatbotMovieRepository.findTopHotMoviesThisWeek(startOfWeek, endOfWeek);
            context.append("PHIM HOT NHẤT TUẦN NÀY (dựa trên số vé bán ra):\n");
            if (!topMovies.isEmpty()) {
                for (int i = 0; i < topMovies.size(); i++) {
                    Movie movie = topMovies.get(i);
                    context.append((i + 1)).append(". ").append(movie.getNameVN()).append(" (")
                            .append(movie.getNameEN())
                            .append(")\n");
                }
            } else {
                context.append("Chưa có dữ liệu phim hot cho tuần này.\n");
            }
        }

        // Hiển thị phim mới nhất
        if (info.askNew) {
            List<Movie> newMovies = chatbotMovieRepository.findNewestMovies();
            context.append("PHIM MỚI NHẤT:\n");
            for (int i = 0; i < Math.min(5, newMovies.size()); i++) {
                Movie movie = newMovies.get(i);
                context.append((i + 1)).append(". ").append(movie.getNameVN()).append(" (").append(movie.getNameEN())
                        .append(")\n");
            }
        }

        // Hiển thị bảng giá vé, giá học sinh, giá nhóm
        if (info.askPrice || info.askStudentDiscount || info.askGroupDiscount) {
            List<FareType> fareTypes = fareTypeRepository.findByIsDeletedFalse();
            context.append("BẢNG GIÁ VÉ:\n");
            for (FareType fare : fareTypes) {
                context.append("- ").append(fare.getName()).append(":\n");
                context.append("  + Giá cơ bản: ").append(fare.getBasePrice()).append(" VND\n");
                context.append("  + Giá theo ngày: ").append(fare.getDayPrice()).append(" VND\n");
                if (fare.getTimeSlotType() != null) {
                    context.append("  + Khung giờ: ").append(fare.getTimeSlotType()).append("\n");
                }
            }
        }

        // Hiển thị thông tin về khuyến mãi
        if (info.askPromotion) {
            List<Promotion> promotions = promotionRepository.findByActiveTrue();
            context.append("KHUYẾN MÃI HIỆN TẠI:\n");
            for (Promotion promo : promotions) {
                context.append("- Mã: ").append(promo.getCode());
                context.append(" - Loại: ").append(promo.getDiscountType());
                context.append(" - Giảm: ").append(promo.getDiscountLevel()).append("%\n");
                if (promo.getMinOrder() != null) {
                    context.append("  + Đơn tối thiểu: ").append(promo.getMinOrder()).append(" VND\n");
                }
            }
        }

        // Hiển thị thông tin về phương thức thanh toán
        if (info.askPayment) {
            List<PaymentMethod> payments = paymentMethodRepository.findByActiveTrue();
            context.append("PHƯƠNG THỨC THANH TOÁN:\n");
            for (PaymentMethod payment : payments) {
                context.append("- ").append(payment.getName()).append("\n");
            }
        }

        // Hiển thị lịch chiếu phim
        if (info.askShowTimes) {
            if (info.movieName != null) {
                // Lịch chiếu cho phim cụ thể - Tìm phim trước, sau đó tìm lịch chiếu
                List<Movie> foundMovies = chatbotMovieRepository.findByNameContaining(info.movieName);
                if (!foundMovies.isEmpty()) {
                    Movie movie = foundMovies.get(0);
                    // Tìm lịch chiếu bằng keyword (LIKE, ignore-case, cả tên VN và EN)
                    List<Screening> movieScreenings = chatbotScreeningRepository.findScreeningsByMovieKeyword(
                            info.movieName, LocalDateTime.now());
                    if (!movieScreenings.isEmpty()) {
                        context.append("LỊCH CHIẾU PHIM '").append(movie.getNameVN()).append("':\n");
                        for (int i = 0; i < Math.min(7, movieScreenings.size()); i++) {
                            Screening screening = movieScreenings.get(i);
                            context.append("- ").append(screening.getShowDateTime().toLocalDate())
                                    .append(" lúc ").append(screening.getShowDateTime().toLocalTime())
                                    .append(" - Phòng ").append(screening.getCinemaRoom().getCinemaRoomName())
                                    .append("\n");
                        }
                    } else {
                        context.append("Hiện tại chưa có lịch chiếu cho phim '").append(movie.getNameVN())
                                .append("'.\n");
                    }
                } else {
                    context.append("Không tìm thấy phim '").append(info.movieName).append("' trong hệ thống.\n");
                }
            } else if (info.date != null && info.date.equals("today")) {
                List<Screening> todayScreenings = chatbotScreeningRepository.findTodayScreenings(LocalDateTime.now());
                if (!todayScreenings.isEmpty()) {
                    context.append("LỊCH CHIẾU HÔM NAY:\n");
                    for (Screening screening : todayScreenings) {
                        context.append("- ").append(screening.getMovie().getNameVN())
                                .append(" lúc ").append(screening.getShowDateTime().toLocalTime())
                                .append(" - Phòng ").append(screening.getCinemaRoom().getCinemaRoomName()).append("\n");
                    }
                } else {
                    context.append("Hiện tại chưa có lịch chiếu nào cho hôm nay.\n");
                }
            } else if (info.date != null && info.date.equals("tomorrow")) {
                List<Screening> tomorrowScreenings = chatbotScreeningRepository
                        .findTomorrowScreenings(LocalDate.now().plusDays(1));
                if (!tomorrowScreenings.isEmpty()) {
                    context.append("LỊCH CHIẾU NGÀY MAI:\n");
                    for (Screening screening : tomorrowScreenings) {
                        context.append("- ").append(screening.getMovie().getNameVN())
                                .append(" lúc ").append(screening.getShowDateTime().toLocalTime())
                                .append(" - Phòng ").append(screening.getCinemaRoom().getCinemaRoomName()).append("\n");
                    }
                } else {
                    context.append("Hiện tại chưa có lịch chiếu nào cho ngày mai.\n");
                }
            }
        }

        // Hiển thị thông tin về rạp chiếu
        if (info.askCinemaInfo) {
            List<CinemaRoom> rooms = chatbotCinemaRepository.findAllByIsDeletedFalse();
            Integer totalSeats = chatbotCinemaRepository.getTotalSeatsCount();
            context.append("THÔNG TIN RẠP CHIẾU:\n");
            context.append("- Tổng số phòng chiếu: ").append(rooms.size()).append("\n");
            context.append("- Tổng số ghế: ").append(totalSeats != null ? totalSeats : 0).append("\n");

            List<CinemaRoom> vipRooms = chatbotCinemaRepository.findVipRooms();
            if (!vipRooms.isEmpty()) {
                context.append("- Có phòng VIP: ");
                for (CinemaRoom room : vipRooms) {
                    context.append(room.getCinemaRoomName()).append(" ");
                }
                context.append("\n");
            }
        }

        //
        if (info.askLocation) {
            context.append("ĐỊA CHỈ RẠP:\n");
            context.append("- MovieTheater Cinema\n");
            context.append("- Địa chỉ: [Địa chỉ cụ thể của rạp]\n");
            context.append("- Gần các khu vực: [Mô tả vị trí]\n");
        }

        // Hiển thị thông tin liên hệ
        if (info.askContact) {
            context.append("THÔNG TIN LIÊN HỆ:\n");
            context.append("- Hotline: 1900-xxxx\n");
            context.append("- Email: support@movietheater.com\n");
            context.append("- Website: www.movietheater.com\n");
            context.append("- Facebook: MovieTheater Official\n");
        }

        // Hiển thị thông tin về giờ hoạt động
        if (info.askOpenHours) {
            context.append("GIỜ HOẠT ĐỘNG:\n");
            context.append("- Thứ 2 - Chủ nhật: 9:00 - 23:00\n");
            context.append("- Suất chiếu cuối: 21:30\n");
            context.append("- Bán vé online 24/7\n");
        }

        // Hiển thị thông tin về phim nếu có (nhưng chưa hiển thị ở phần lịch chiếu)
        if (info.movieName != null && !info.askShowTimes) {
            List<Movie> foundMovies = chatbotMovieRepository.findByNameContaining(info.movieName);
            if (!foundMovies.isEmpty()) {
                context.append("THÔNG TIN PHIM '").append(info.movieName.toUpperCase()).append("':\n");
                for (Movie movie : foundMovies) {
                    context.append("- Tên: ").append(movie.getNameVN()).append(" (").append(movie.getNameEN())
                            .append(")\n");
                    context.append("- Thời lượng: ").append(movie.getDuration()).append(" phút\n");
                    context.append("- Đạo diễn: ").append(movie.getDirector()).append("\n");
                    context.append("- Giới hạn tuổi: ").append(movie.getAgeLimit()).append("\n");
                    // Nếu intent là hỏi nội dung/tóm tắt phim, chỉ trả lời ngắn gọn, không spoil
                    // chi tiết
                    if (info.askContent) {
                        context.append("- Nội dung: ");
                        if (movie.getContent() != null && !movie.getContent().isEmpty()) {
                            // Lấy tối đa 200 ký tự đầu, không cắt giữa câu
                            String content = movie.getContent();
                            int cut = Math.min(content.length(), 200);
                            int lastDot = content.lastIndexOf('.', cut);
                            if (lastDot > 50)
                                cut = lastDot + 1;
                            context.append(content.substring(0, cut).trim());
                            if (cut < content.length())
                                context.append(" ...");
                        } else {
                            context.append("Chưa có nội dung tóm tắt.");
                        }
                        context.append("\n");
                    } else {
                        context.append("- Nội dung: ").append(movie.getContent()).append("\n");
                    }

                    // Chỉ hiển thị lịch chiếu nếu người dùng không hỏi riêng về lịch chiếu
                    List<Screening> screenings = chatbotScreeningRepository.findByMovieName(info.movieName,
                            LocalDateTime.now());
                    if (!screenings.isEmpty()) {
                        context.append("- Lịch chiếu sắp tới:\n");
                        for (int i = 0; i < Math.min(3, screenings.size()); i++) {
                            Screening screening = screenings.get(i);
                            context.append("  + ").append(screening.getShowDateTime().toLocalDate())
                                    .append(" lúc ").append(screening.getShowDateTime().toLocalTime())
                                    .append(" - Phòng ").append(screening.getCinemaRoom().getCinemaRoomName())
                                    .append("\n");
                        }
                    }
                }
            } else {
                // Nếu không tìm thấy phim, trả về context rõ ràng, không để AI tự bịa nội dung
                context.append("Xin lỗi, hiện tại hệ thống chưa có thông tin về phim này.\n");
            }
        }

        // ===== XỬ LÝ ĐẶC BIỆT: Nếu chỉ hỏi về giờ hoạt động, trả về context ngắn gọn
        // =====
        if (isOnlyIntent(info, i -> i.askOpenHours)) {
            return "GIỜ HOẠT ĐỘNG:\n" +
                    "- Thứ 2 - Chủ nhật: 9:00 - 23:00\n" +
                    "- Suất chiếu cuối: 21:30\n" +
                    "- Bán vé online 24/7\n";
        }

        // ===== XỬ LÝ ĐẶC BIỆT: Nếu chỉ hỏi lịch chiếu phim hôm nay, trả về đúng
        // context =====

        if (isOnlyIntent(info, i -> i.askShowTimes && "today".equals(i.date))) {
            List<Screening> todayScreenings = chatbotScreeningRepository.findTodayScreenings(LocalDateTime.now());
            if (!todayScreenings.isEmpty()) {
                StringBuilder todayContext = new StringBuilder();
                todayContext.append("LỊCH CHIẾU HÔM NAY:\n");
                for (Screening screening : todayScreenings) {
                    todayContext.append("- ").append(screening.getMovie().getNameVN())
                            .append(" lúc ").append(screening.getShowDateTime().toLocalTime())
                            .append(" - Phòng ").append(screening.getCinemaRoom().getCinemaRoomName()).append("\n");
                }
                return todayContext.toString();
            } else {
                return "Hiện tại chưa có lịch chiếu nào cho hôm nay.\n";
            }
        }

        if (info.askChangeTicket || info.askRefund) {
            return "Theo quy định của MovieTheater, sau khi đặt vé thành công, khách hàng KHÔNG thể đổi suất chiếu hoặc hủy/hoàn vé. Tuy nhiên, bạn có thể chuyển vé cho người khác sử dụng.";
        }

        // Account-related responses
        if (info.askForgotPassword) {
            return "HƯỚNG DẪN LẤY LẠI MẬT KHẨU:\n" +
                    "1. Truy cập trang đăng nhập của MovieTheater\n" +
                    "2. Nhấp vào liên kết 'Quên mật khẩu?'\n" +
                    "3. Nhập email đã đăng ký tài khoản\n" +
                    "4. Kiểm tra email và làm theo hướng dẫn để đặt lại mật khẩu\n" +
                    "5. Đăng nhập bằng mật khẩu mới\n\n" +
                    "Nếu gặp khó khăn, vui lòng liên hệ hotline: 1900-xxxx để được hỗ trợ.";
        }

        if (info.askRegister) {
            return "HƯỚNG DẪN ĐĂNG KÝ TÀI KHOẢN MOVIETHEATER:\n" +
                    "1. Tại trang chủ nhấp vào nút 'Đăng nhập' ở góc phải màn hình\n" +
                    "2. Nhấp vào nút 'Đăng ký' màu đỏ\n" +
                    "3. Điền đầy đủ thông tin yêu cầu\n" +
                    "4. Nhấn 'Đăng ký' để hoàn tất\n" +
                    "5. Kiểm tra email để xác thực tài khoản\n\n" +
                    "Lưu ý: Tài khoản thành viên sẽ được tích điểm và nhận nhiều ưu đãi hấp dẫn!";
        }

        if (info.askUpdateProfile) {
            return "HƯỚNG DẪN CẬP NHẬT THÔNG TIN CÁ NHÂN:\n" +
                    "1. Đăng nhập vào tài khoản MovieTheater\n" +
                    "2. Nhấp vào avatar/tên của bạn ở góc phải màn hình\n" +
                    "3. Chọn 'Tài khoản'\n" +
                    "4. Chỉnh sửa các thông tin cần thiết:\n" +
                    "   - Họ tên, số điện thoại, email\n" +
                    "   - Ngày sinh, giới tính\n" +
                    "   - Địa chỉ\n" +
                    "   - Ảnh đại diện\n" +
                    "5. Nhấn 'Cập nhật' để lưu thay đổi\n\n" +
                    "Lưu ý: Một số thông tin như email có thể cần xác thực lại qua email mới.";
        }

        return context.toString();
    }
}
