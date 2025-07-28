//package com.example.demo.utils;
//
//import java.util.regex.Matcher;
//import java.util.regex.Pattern;
//
///**
// * QuestionAnalyzer - Phân tích câu hỏi của người dùng để xác định intent
// * <p>
// * Chức năng:
// * - Nhận diện intent/chủ đề từ câu hỏi của người dùng
// * - Trích xuất thông tin cơ bản: tên phim, thể loại, thời gian, ngày
// * - Sử dụng 20 intent chuẩn hóa theo mức độ ưu tiên
// * <p>
// * Cách thêm intent mới:
// * 1. Thêm biến boolean mới vào class QueryInfo
// * 2. Cập nhật hàm hasAnyIntent() để bao gồm intent mới
// * 3. Thêm logic nhận diện từ khóa trong hàm analyze()
// * 4. Đảm bảo thứ tự ưu tiên phù hợp
// * <p>
// * Cách cập nhật logic nhận diện:
// * - Thêm từ khóa mới vào các điều kiện if
// * - Sử dụng regex cho pattern phức tạp
// * - Test kỹ để tránh conflict giữa các intent
// *
// * @author MovieTheater Team
// * @version 2.0 - Refactored và chuẩn hóa intent
// */
//public class QuestionAnalyzer {
//    public static class QueryInfo {
//
//        // Thông tin cơ bản
//        public String genre; // Thể loại phim
//        public String time; // Thời gian cụ thể (ví dụ: "19:00", "09:00")
//        public String date; // Ngày cụ thể (ví dụ: "2023-10-01", "01/10/2023", "next_week", "today",
//        // "tomorrow")
//        public String movieName; // Tên phim nếu có
//
//        // Intent chuẩn hóa - Sắp xếp theo mức độ quan trọng
//        // 1. Core Business (Quan trọng nhất)
//        public boolean movie_showtimes; // Lịch chiếu, suất chiếu, thời gian chiếu phim
//        public boolean movie_now_showing; // Phim đang chiếu hiện tại
//        public boolean movie_booking; // Đặt vé, mua vé, booking online
//        public boolean movie_pricing; // Giá vé, bảng giá, chi phí xem phim
//        public boolean movie_info; // Thông tin chi tiết phim: nội dung, thời lượng, diễn viên, đạo diễn
//        public boolean movie_coming_soon; // Phim sắp chiếu, phim mới sắp ra mắt
//        public boolean movie_release_date; // Ngày khởi chiếu phim
//
//        // 2. Hỗ trợ quyết định
//        public boolean seat_selection; // Chọn ghế, loại ghế (VIP, thường, đôi)
//        public boolean movie_genre; // Phim theo thể loại: hành động, hài, kinh dị, hoạt hình
//        public boolean promotion_discount; // Khuyến mãi, giảm giá, ưu đãi
//        public boolean payment_methods; // Phương thức thanh toán: thẻ, ví điện tử, tiền mặt
//        public boolean cinema_info; // Thông tin rạp: cơ sở vật chất, số phòng, trang thiết bị
//        public boolean movie_language; // Ngôn ngữ phim: phụ đề, lồng tiếng, ngôn ngữ gốc
//        public boolean movie_technology; // Công nghệ chiếu: 3D, IMAX, Dolby
//
//        // 3. Thông tin dịch vụ
//        public boolean cinema_location; // Địa chỉ rạp, cách đi, vị trí
//        public boolean operating_hours; // Giờ mở cửa, giờ đóng cửa, thời gian hoạt động
//        public boolean movie_reviews; // Đánh giá phim, review, rating
//        public boolean contact_support; // Thông tin liên hệ, hỗ trợ khách hàng
//
//        // 4. Hỗ trợ kỹ thuật
//        public boolean account_management; // Đăng ký tài khoản, cập nhật thông tin, quên mật khẩu
//        public boolean ticket_policy; // Chính sách đổi vé, hủy vé, hoàn tiền
//        public boolean technical_support; // Hỗ trợ kỹ thuật, báo lỗi hệ thống
//
//        // Kiểm tra có intent nào được bật
//        public boolean hasAnyIntent() {
//            return genre != null ||
//            // Core Business
//                    movie_showtimes || movie_now_showing || movie_booking || movie_pricing ||
//                    movie_info || movie_coming_soon || movie_release_date ||
//                    // Hỗ trợ quyết định
//                    seat_selection || movie_genre || promotion_discount || payment_methods ||
//                    cinema_info || movie_language || movie_technology ||
//                    // Thông tin dịch vụ
//                    cinema_location || operating_hours || movie_reviews || contact_support ||
//                    // Hỗ trợ kỹ thuật
//                    account_management || ticket_policy || technical_support;
//        }
//
//        @Override
//        public String toString() {
//            return "QueryInfo{" +
//                    "genre='" + genre + '\'' +
//                    ", time='" + time + '\'' +
//                    ", date='" + date + '\'' +
//                    ", movieName='" + movieName + '\'' +
//                    ", movie_showtimes=" + movie_showtimes +
//                    ", movie_now_showing=" + movie_now_showing +
//                    ", movie_booking=" + movie_booking +
//                    ", movie_pricing=" + movie_pricing +
//                    ", movie_info=" + movie_info +
//                    ", movie_coming_soon=" + movie_coming_soon +
//                    ", movie_release_date=" + movie_release_date +
//                    ", seat_selection=" + seat_selection +
//                    ", movie_genre=" + movie_genre +
//                    ", promotion_discount=" + promotion_discount +
//                    ", payment_methods=" + payment_methods +
//                    ", cinema_info=" + cinema_info +
//                    ", movie_language=" + movie_language +
//                    ", movie_technology=" + movie_technology +
//                    ", cinema_location=" + cinema_location +
//                    ", operating_hours=" + operating_hours +
//                    ", movie_reviews=" + movie_reviews +
//                    ", contact_support=" + contact_support +
//                    ", account_management=" + account_management +
//                    ", ticket_policy=" + ticket_policy +
//                    ", technical_support=" + technical_support +
//                    '}';
//        }
//    }
//
//    public static QueryInfo analyze(String question) {
//        QueryInfo info = new QueryInfo();
//        String q = question.toLowerCase();
//
//        // Chào hỏi chung - không cần truy vấn phim
//        if (q.matches(".*\\b(xin chào|chào|hello|hi|chào bạn|chào em)\\b.*") ||
//                q.matches(".*\\b(cảm ơn|thank you|thanks)\\b.*") ||
//                q.matches(".*\\b(tạm biệt|bye|goodbye)\\b.*")) {
//            return info; // Trả về info rỗng, không có intent cụ thể
//        }
//
//        // ====== THÔNG TIN CƠ BẢN ======
//        // Thể loại phim: cải thiện nhận diện chính xác
//        // Pattern 1: "thể loại [tên thể loại]" hoặc "genre [tên thể loại]" - cải thiện
//        // chính xác hơn
//        Pattern genrePatternFull = Pattern.compile("(?:thể loại|genre)\\s*(?:là)?\\s*([a-zA-ZÀ-ỹ ]{4,30})",
//                Pattern.UNICODE_CASE);
//        Matcher genreMatcherFull = genrePatternFull.matcher(q);
//        if (genreMatcherFull.find()) {
//            String genreCandidate = genreMatcherFull.group(1).trim();
//            System.out.println(">>> Genre candidate from pattern full: " + genreCandidate);
//            if (genreCandidate.length() >= 4 && !genreCandidate.matches("^(gì|nào|của|cho|về|trong|nào)$")) {
//                info.genre = genreCandidate;
//            }
//        }
//
//        // Pattern 2: "phim [thể loại]" - chỉ áp dụng cho các thể loại phổ biến
//        if (info.genre == null) {
//            if (q.matches(".*\\b(hoạt hình|animation)\\b.*"))
//                info.genre = "Hoạt hình";
//            else if (q.matches(".*\\b(hành động|action)\\b.*"))
//                info.genre = "Hành động";
//            else if (q.matches(".*\\b(tình cảm|romance)\\b.*"))
//                info.genre = "Tình cảm";
//            else if (q.matches(".*\\b(kinh dị|horror)\\b.*"))
//                info.genre = "Kinh dị";
//            else if (q.matches(".*\\b(hài|comedy)\\b.*"))
//                info.genre = "Hài";
//            else if (q.matches(".*\\b(phiêu lưu|adventure)\\b.*"))
//                info.genre = "Phiêu lưu";
//            else if (q.matches(".*\\b(khoa học viễn tưởng|sci-fi|siêu anh hùng|marvel|dc)\\b.*"))
//                info.genre = "Khoa học viễn tưởng";
//        }
//
//        // Thời gian cụ thể
//        if (q.contains("tối nay"))
//            info.time = "19:00";
//        else if (q.contains("sáng mai"))
//            info.time = "09:00";
//        else if (q.contains("chiều nay"))
//            info.time = "15:00";
//        else if (q.contains("trưa nay"))
//            info.time = "12:00";
//        else if (q.contains("tối mai"))
//            info.time = "19:00";
//
//        // Ngày cụ thể
//        if (q.contains("cuối tuần"))
//            info.date = "weekend";
//
//        // Ngày (regex pattern)
//        Pattern datePattern = Pattern.compile("(\\d{1,2}[-/\\.]\\d{1,2}[-/\\.]\\d{2,4})");
//        Matcher matcher = datePattern.matcher(q);
//        if (matcher.find()) {
//            info.date = matcher.group(1);
//        }
//
//        // Nhận diện giờ cụ thể (HH:mm hoặc HHh)
//        Pattern timePattern = Pattern.compile("(\\d{1,2})[:h]\\s*(\\d{0,2})");
//        Matcher timeMatcher = timePattern.matcher(q);
//        if (timeMatcher.find()) {
//            String hour = timeMatcher.group(1);
//            String minute = timeMatcher.group(2);
//            if (minute.isEmpty())
//                minute = "00";
//            info.time = String.format("%02d:%02s", Integer.parseInt(hour), minute);
//        }
//
//        // Tên phim (nhiều pattern) - Cải thiện để tránh lấy nhầm câu hỏi
//        // Pattern 1 cải tiến: "phim [tên phim]" - chỉ lấy tên phim thật, tránh các câu
//        // hỏi về danh sách
//        Pattern moviePattern1 = Pattern.compile(
//                "phim ([a-zA-Z0-9\u00C0-\u1EF9\s:&'\"!.,-]+?)(?:\\s(?:là|có|được|bao|khi|nào|gì|đang chiếu|sắp chiếu|mới|hot|hay|top|trending|nay|hôm|hôm nay|hôm qua|mai|ngày mai|tuần|tháng|làm sao để|cách|hướng dẫn|bạn có thể|cung cấp|như thế nào|không|bạn|có thể loại|nào sắp|phim nào|phim có)|\\?|\\.|,|$)");
//        Matcher movieMatcher1 = moviePattern1.matcher(q);
//        if (movieMatcher1.find()) {
//            String candidate = movieMatcher1.group(1).trim();
//            System.out.println(">>> Candidate movie name from pattern 1: " + candidate);
//            // Loại trừ các câu hỏi về danh sách phim và thể loại
//            if (candidate.length() > 3 &&
//                    !candidate.matches(
//                            "^(đang|sắp|mới|hot|hay|nào|chiếu|là|này|gì|top|trending|sắp chiếu|nay|hôm|hôm nay|hôm qua|mai|ngày mai|tuần|tháng|làm sao để|cách|hướng dẫn|bạn có thể|cung cấp|như thế nào|đang chiếu|đang chiếu không|phim gì đang chiếu|phim đang chiếu|phim sắp chiếu|phim mới|bạn|có thể loại|nào sắp|phim nào|phim có|hãy cho tôi biết những bộ|hãy cho tôi biết|cho tôi biết những bộ|cho tôi biết).*$")
//                    &&
//                    !q.contains("những bộ phim") && !q.contains("phim nào") && !q.contains("danh sách")) {
//                info.movieName = candidate;
//            }
//        }
//
//        // Pattern 2: "[tên phim] có/được/là" - cải tiến cho các câu hỏi khác
//        Pattern moviePattern2 = Pattern.compile("([a-zA-Z0-9\u00C0-\u1EF9\s:&'\"!.,-]{4,25})\\s+(?:có|được|là)\\s");
//        Matcher movieMatcher2 = moviePattern2.matcher(q);
//        if (movieMatcher2.find() && info.movieName == null) {
//            String candidate = movieMatcher2.group(1).trim();
//            System.out.println(">>> Candidate movie name from pattern 2: " + candidate);
//            // Loại trừ các từ hỏi và câu về danh sách
//            if (!candidate.matches(
//                    "^(đang|sắp|mới|hot|hay|nào|chiếu|là|này|gì|top|trending|sắp chiếu|nay|hôm|hôm nay|hôm qua|mai|ngày mai|tuần|tháng|làm sao để|cách|hướng dẫn|bạn có thể|cung cấp|như thế nào|đang chiếu|đang chiếu không|phim gì đang chiếu|phim đang chiếu|phim sắp chiếu|phim mới|bạn|những bộ phim|hãy cho tôi biết|cho tôi biết).*$")
//                    &&
//                    !q.contains("những bộ phim") && !q.contains("phim nào") && !q.contains("danh sách")) {
//                // Loại bỏ các từ đầu không phải tên phim
//                candidate = candidate
//                        .replaceFirst("^(?:thông tin|ngày khởi chiếu|lịch chiếu|suất chiếu|giá vé|bạn|tôi)\\s+", "");
//                info.movieName = candidate;
//            }
//        }
//
//        // Pattern 3: Tên phim trong dấu ngoặc kép
//        Pattern moviePattern3 = Pattern.compile("\"([^\"]+)\"");
//        Matcher movieMatcher3 = moviePattern3.matcher(q);
//        if (movieMatcher3.find() && info.movieName == null) {
//            System.out.println(">>> Found movie name in quotes: " + movieMatcher3.group(1));
//            info.movieName = movieMatcher3.group(1).trim();
//        }
//
//        // Pattern 4: "[tên phim] khi nào chiếu" - cho trường hợp câu hỏi về lịch chiếu
//        Pattern moviePattern4 = Pattern.compile("^([a-zA-Z0-9\u00C0-\u1EF9\s:&'\"!.,-]+?)\\s+khi\\s+nào\\s+chiếu");
//        Matcher movieMatcher4 = moviePattern4.matcher(q);
//        if (movieMatcher4.find() && info.movieName == null) {
//            String candidate = movieMatcher4.group(1).trim();
//            System.out.println(">>> Candidate movie name from pattern 4 (khi nào chiếu): " + candidate);
//            // Nếu candidate là 1 từ ngắn (<=3 ký tự) hoặc nằm trong danh sách loại trừ thì
//            // bỏ qua
//            if (candidate.length() > 3 && !candidate.matches(
//                    "^(đang|sắp|mới|hot|hay|nào|chiếu|là|này|gì|top|trending|sắp chiếu|nay|hôm|hôm nay|hôm qua|mai|ngày mai|tuần|tháng|làm sao để|cách|hướng dẫn|bạn có thể|cung cấp|như thế nào|đang chiếu|đang chiếu không|phim gì đang chiếu|phim đang chiếu|phim sắp chiếu|phim mới|bạn|phim)$")) {
//                info.movieName = candidate;
//            }
//        }
//
//        // ====== INTENT RECOGNITION (Theo thứ tự ưu tiên) ======
//
//        // 1. MOVIE_SHOWTIMES - Lịch chiếu, suất chiếu
//        if (q.contains("suất chiếu") || q.contains("giờ chiếu") || q.contains("chiếu lúc mấy giờ") ||
//                q.contains("lịch chiếu") || q.contains("thời gian chiếu") || q.contains("chiếu vào") ||
//                q.contains("showtime") || q.contains("schedule") || q.contains("khi nào chiếu"))
//            info.movie_showtimes = true;
//
//        // 2. MOVIE_NOW_SHOWING - Phim đang chiếu (gộp askNowShowing, askTop, askNew)
//        if (q.contains("phim đang chiếu") || q.contains("phim gì đang chiếu") || q.contains("now showing") ||
//                q.contains("phim hot") || q.contains("phim top") || q.contains("phim trending") ||
//                q.contains("phim mới") || q.contains("phim nào hay") || q.contains("đang được chiếu"))
//            info.movie_now_showing = true;
//        // Nếu câu hỏi có từ 'thông tin' và 'phim đang chiếu' thì bật luôn movie_info
//        if ((q.contains("thông tin") || q.contains("chi tiết") || q.contains("cung cấp thông tin")) &&
//                (q.contains("phim đang chiếu") || q.contains("phim gì đang chiếu") || q.contains("now showing"))) {
//            info.movie_info = true;
//        }
//
//        // 3. MOVIE_BOOKING - Đặt vé, booking (mở rộng nhận diện)
//        if (q.matches(".*(đặt|mua)\\s*(vé|ve).*") ||
//                q.contains("booking") ||
//                q.contains("đặt chỗ") ||
//                q.contains("reserv") ||
//                q.matches(".*(làm sao|cách|hướng dẫn|làm thế nào|như thế nào|muốn|có thể).*(đặt|mua|book)\\s*(vé|ve).*")
//                ||
//                q.matches(".*(đặt|mua|book)\\s*(vé|ve).*(như thế nào|làm sao|cách|hướng dẫn|làm thế nào|có thể).*") ||
//                q.contains("hướng dẫn đặt vé") ||
//                q.contains("cách đặt vé") ||
//                q.contains("muốn đặt vé") ||
//                q.contains("đặt vé như thế nào") ||
//                q.contains("làm sao để đặt vé") ||
//                q.contains("làm sao để có thể đặt được vé")) {
//            info.movie_booking = true;
//        }
//
//        // 4. MOVIE_PRICING - Giá vé (gộp askPrice)
//        if (q.contains("giá vé") || q.contains("bao nhiêu tiền") || q.contains("mắc không") ||
//                q.contains("rẻ không") || q.contains("bảng giá") || q.contains("chi phí") ||
//                q.contains("price") || q.contains("cost"))
//            info.movie_pricing = true;
//
//        // 5. MOVIE_INFO - Thông tin phim (gộp askDirector, askActor, askDuration,
//        // askContent, askTrailer, askAgeLimit)
//        if (q.contains("đạo diễn") || q.contains("director") ||
//                q.contains("diễn viên") || q.contains("actor") || q.contains("cast") ||
//                q.contains("thời lượng") || q.contains("bao lâu") || q.contains("dài không") || q.contains("duration")
//                ||
//                q.contains("nội dung") || q.contains("tóm tắt") || q.contains("content") || q.contains("plot") ||
//                q.contains("trailer") || q.contains("xem thử") ||
//                q.contains("giới hạn tuổi") || q.contains("độ tuổi") || q.contains("age limit") ||
//                q.matches(".*(thông tin|chi tiết).*(phim|phim này).*") || q.contains("thông tin của"))
//            info.movie_info = true;
//
//        // 6. MOVIE_COMING_SOON - Phim sắp chiếu (mở rộng nhận diện)
//        if (q.contains("phim sắp chiếu") || q.contains("sắp ra mắt") || q.contains("coming soon") ||
//                q.contains("phim mới sắp") || q.contains("sắp ra rạp") || q.contains("upcoming") ||
//                q.contains("sắp được chiếu") || q.contains("phim nào sắp") ||
//                q.matches(".*phim\\s+nào\\s+sắp.*") || q.contains("thời gian tới") ||
//                q.contains("sắp có phim") || q.contains("trong thời gian tới"))
//            info.movie_coming_soon = true;
//
//        // 7. SEAT_SELECTION - Chọn ghế (gộp askSeatSelection)
//        if (q.contains("chọn ghế") || q.contains("ghế vip") || q.contains("ghế đôi") ||
//                q.contains("ghế couple") || q.contains("seat selection") || q.contains("ghế hàng") ||
//                q.contains("chỗ ngồi") || q.contains("loại ghế"))
//            info.seat_selection = true;
//
//        // 8. MOVIE_GENRE - Thể loại phim (sử dụng info.genre đã set ở trên)
//        if (info.genre != null || q.contains("thể loại") || q.contains("genre") ||
//                q.contains("loại phim") || q.contains("category"))
//            info.movie_genre = true;
//
//        // 9. PROMOTION_DISCOUNT - Khuyến mãi (mở rộng nhận diện)
//        if (q.contains("khuyến mãi") || q.contains("khuyến mãi gì") || q.contains("khuyến mãi nào")
//                || q.contains("ưu đãi") || q.contains("giảm giá") ||
//                q.contains("promotion") || q.contains("discount") || q.contains("voucher") ||
//                q.contains("giảm giá nhóm") || q.contains("group discount") ||
//                q.contains("giảm giá học sinh") || q.contains("student discount") ||
//                q.contains("mã giảm giá") || q.contains("coupon") ||
//                q.contains("chương trình khuyến mãi") || q.contains("chương trình ưu đãi") ||
//                q.contains("có khuyến mãi") || q.contains("có ưu đãi") ||
//                q.contains("những chương trình khuyến mãi") || q.contains("các chương trình khuyến mãi") ||
//                q.contains("chương trình khuyến mãi hiện tại") || q.contains("chương trình khuyến mãi đang có") ||
//                q.matches(".*(?:có|đang có|hiện tại|bây giờ|rạp).*chương trình.*khuyến mãi.*") ||
//                q.matches(".*chương trình khuyến mãi gì.*") ||
//                q.matches(".*chương trình gì.*khuyến mãi.*") ||
//                q.matches(".*có chương trình.*khuyến mãi.*") ||
//                q.matches(".*(những|các) chương trình khuyến mãi.*") ||
//                q.matches(".*chương trình khuyến mãi (hiện tại|đang có).*") ||
//                q.contains("chương trình gì") || q.contains("có chương trình nào"))
//            info.promotion_discount = true;
//
//        // 10. PAYMENT_METHODS - Phương thức thanh toán (gộp askPayment)
//        // Chỉ nhận diện 'thẻ' tiếng Việt, không nhận diện 'the' trong tên phim tiếng
//        // Anh
//        // Sử dụng regex chặt chẽ hơn để tránh nhầm lẫn với "the" tiếng Anh
//        boolean hasTheVietnamese = Pattern.compile("(?<![a-zA-Z])thẻ(?![a-zA-Z])", Pattern.UNICODE_CASE).matcher(q)
//                .find();
//        if ((q.contains("thanh toán") || q.contains("payment") || q.contains("ví điện tử") ||
//                q.contains("momo") || q.contains("zalopay") || hasTheVietnamese ||
//                q.contains("visa") || q.contains("mastercard") || q.contains("atm") ||
//                q.contains("tiền mặt") || q.contains("cash") || q.contains("chuyển khoản")) &&
//                !(q.contains("suất chiếu") || q.contains("lịch chiếu") || q.contains("giờ chiếu")
//                        || q.contains("phim"))) {
//            info.payment_methods = true;
//        }
//
//        // 11. CINEMA_INFO - Thông tin rạp (gộp askCinemaInfo)
//        if (q.contains("thông tin rạp") || q.contains("rạp") || q.contains("cinema") || q.contains("theater") ||
//                q.contains("cơ sở vật chất") || q.contains("trang thiết bị") || q.contains("phòng chiếu") ||
//                q.contains("số ghế") || q.contains("màn hình") || q.contains("âm thanh"))
//            info.cinema_info = true;
//
//        // 12. MOVIE_LANGUAGE - Ngôn ngữ phim (gộp askLanguage, askSubtitle, askDubbing)
//        if (q.contains("ngôn ngữ") || q.contains("tiếng gì") || q.contains("language") ||
//                q.contains("phụ đề") || q.contains("subtitle") ||
//                q.contains("lồng tiếng") || q.contains("dubbing") || q.contains("thuyết minh") ||
//                q.contains("tiếng việt") || q.contains("tiếng anh"))
//            info.movie_language = true;
//
//        // 13. MOVIE_TECHNOLOGY - Công nghệ chiếu (gộp ask3D, askIMAX)
//        if (q.contains("3d") || q.contains("imax") || q.contains("dolby") ||
//                q.contains("công nghệ") || q.contains("technology") || q.contains("4dx") ||
//                q.contains("kính 3d") || q.contains("hiệu ứng"))
//            info.movie_technology = true;
//
//        // 14. CINEMA_LOCATION - Địa chỉ rạp (gộp askLocation)
//        if (q.contains("địa chỉ") || q.contains("ở đâu") || q.contains("location") ||
//                q.contains("đường đi") || q.contains("cách đi") || q.contains("vị trí") ||
//                q.contains("address") || q.contains("directions"))
//            info.cinema_location = true;
//
//        // 15. OPERATING_HOURS - Giờ hoạt động (gộp askOpenHours)
//        if (q.contains("giờ mở cửa") || q.contains("giờ đóng cửa") || q.contains("operating hours") ||
//                q.contains("giờ hoạt động") || q.contains("thời gian hoạt động") || q.contains("working hours") ||
//                q.contains("mở cửa") || q.contains("đóng cửa"))
//            info.operating_hours = true;
//
//        // 16. MOVIE_REVIEWS - Đánh giá phim (gộp askReview)
//        if ((q.contains("đánh giá") || q.contains("review") || q.contains("rating") ||
//                q.contains("nhận xét") || q.contains("feedback")) ||
//                (q.contains("phim") && (q.contains("hay không") || q.contains("có hay không"))))
//            info.movie_reviews = true;
//
//        // 17. CONTACT_SUPPORT - Liên hệ hỗ trợ (gộp askContact)
//        if (q.contains("liên hệ") || q.contains("contact") || q.contains("hỗ trợ") || q.contains("support") ||
//                q.contains("hotline") || q.contains("số điện thoại") || q.contains("email") ||
//                q.contains("facebook") || q.contains("website"))
//            info.contact_support = true;
//
//        // 18. ACCOUNT_MANAGEMENT - Quản lý tài khoản (cải thiện nhận diện chính xác)
//        if ((q.contains("quên mật khẩu") || q.contains("quên password") || q.contains("forgot password") ||
//                q.contains("reset password") || q.contains("đặt lại mật khẩu") || q.contains("lấy lại mật khẩu") ||
//                q.contains("đăng ký") || q.contains("đăng kí") || q.contains("tạo tài khoản") ||
//                q.contains("register") || q.contains("sign up") || q.contains("tài khoản mới") ||
//                q.contains("cập nhật thông tin") || q.contains("chỉnh sửa thông tin") ||
//                q.contains("thay đổi thông tin") || q.contains("update profile") || q.contains("edit profile") ||
//                q.contains("sửa hồ sơ") || q.contains("chỉnh lại thông tin cá nhân")
//                || q.contains("cập nhật thông tin cá nhân") ||
//                q.contains("đăng ký tài khoản mới") || q.contains("đăng ký mới") || q.contains("đăng kí mới") ||
//                q.contains("tạo tài khoản mới") || q.contains("làm sao để đăng ký tài khoản")
//                || q.contains("làm sao để lấy lại mật khẩu"))
//                && !q.contains("phim") && !q.contains("vé") && !q.contains("lịch chiếu") // Loại trừ context về phim
//        ) {
//            info.account_management = true;
//        }
//
//        // 19. TICKET_POLICY - Chính sách vé (gộp askChangeTicket, askRefund)
//        if (q.contains("đổi vé") || q.contains("đổi suất") || q.contains("thay đổi suất") ||
//                q.contains("change ticket") || q.contains("change showtime") ||
//                q.contains("hủy vé") || q.contains("huỷ vé") || q.contains("hủy đặt vé") ||
//                q.contains("hoàn tiền") || q.contains("refund") || q.contains("trả lại vé") ||
//                q.contains("cancel ticket") || q.contains("chính sách") || q.contains("policy"))
//            info.ticket_policy = true;
//
//        // 20. TECHNICAL_SUPPORT - Hỗ trợ kỹ thuật (gộp askError, askHelp)
//        if (q.contains("lỗi") || q.contains("error") || q.contains("bug") ||
//                q.contains("không hoạt động") || q.contains("bị lỗi") || q.contains("crash") ||
//                q.contains("hỗ trợ kỹ thuật") || q.contains("technical support") ||
//                q.contains("help") || q.contains("giúp đỡ") || q.contains("cần hỗ trợ"))
//            info.technical_support = true;
//
//        // 19. MOVIE_RELEASE_DATE - Ngày khởi chiếu phim
//        if (q.contains("khởi chiếu") || q.contains("ra mắt") || q.contains("bắt đầu chiếu")
//                || q.contains("release date") || q.contains("ngày chiếu") || q.contains("chiếu từ ngày") ||
//                (q.contains("khi nào chiếu") && info.movieName != null)) {
//            info.movie_release_date = true;
//        }
//
//        return info;
//    }
//
//    /*
//     * ====== HƯỚNG DẪN BẢO TRÌ VÀ MỞ RỘNG ======
//     *
//     * 1. Thêm Intent mới:
//     * - Bước 1: Thêm biến boolean vào QueryInfo class
//     * - Bước 2: Cập nhật hasAnyIntent() method
//     * - Bước 3: Thêm logic nhận diện trong analyze() method
//     * - Bước 4: Test kỹ để đảm bảo không conflict
//     *
//     * 2. Cập nhật từ khóa:
//     * - Thêm từ khóa mới vào điều kiện if tương ứng
//     * - Sử dụng || để nối các điều kiện
//     * - Ưu tiên từ khóa tiếng Việt trước, tiếng Anh sau
//     *
//     * 3. Thứ tự ưu tiên Intent:
//     * Core Business > Hỗ trợ quyết định > Thông tin dịch vụ > Hỗ trợ kỹ thuật
//     *
//     * 4. Pattern phức tạp:
//     * - Sử dụng regex Pattern và Matcher cho trường hợp phức tạp
//     * - Đặt pattern matching sau basic string matching
//     *
//     * 5. Testing:
//     * - Test với các câu hỏi thực tế từ người dùng
//     * - Đảm bảo intent được nhận diện chính xác
//     * - Kiểm tra không bị chồng chéo giữa các intent
//     */
//}
