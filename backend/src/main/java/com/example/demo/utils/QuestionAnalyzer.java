package com.example.demo.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class QuestionAnalyzer {
    public static class QueryInfo {
        public String genre; // Thể loại phim
        public String time; // Thời gian cụ thể (ví dụ: "19:00", "09:00")
        public String date; // Ngày cụ thể (ví dụ: "2023-10-01", "01/10/2023", "next_week", "today",
                            // "tomorrow")
        public String movieName; // Tên phim nếu có
        public boolean askPromotion; // Hỏi về khuyến mãi
        public boolean askDirector; // Hỏi về đạo diễn
        public boolean askActor; // Hỏi về diễn viên
        public boolean askDuration; // Hỏi về thời lượng phim
        public boolean askPrice; // Hỏi về giá vé
        public boolean askShowTimes; // Hỏi về suất chiếu
        public boolean askLanguage; // Hỏi về ngôn ngữ phim
        public boolean askAgeLimit; // Hỏi về giới hạn tuổi
        public boolean askTrailer; // Hỏi về trailer

        // Thêm các intent mới
        public boolean askBooking; // Đặt vé
        public boolean askReview; // Đánh giá phim
        public boolean askCinemaInfo; // Thông tin rạp

        // public boolean askFood; // Đồ ăn/nước uống
        // public boolean askParking; // Chỗ đỗ xe
        // public boolean askVipRoom; // Phòng VIP

        public boolean askPayment; // Thanh toán

        public boolean askChangeTicket; // Đổi vé/suất chiếu
        public boolean askRefund; // Hủy vé/hoàn tiền

        public boolean askSeatSelection; // Chọn ghế
        public boolean askGroupDiscount; // Giảm giá nhóm
        public boolean askStudentDiscount; // Giảm giá học sinh
        public boolean askLocation; // Địa chỉ rạp
        public boolean askContact; // Liên hệ
        public boolean askOpenHours; // Giờ mở cửa
        public boolean askComingSoon; // Phim sắp chiếu
        public boolean askNowShowing; // Phim đang chiếu
        public boolean askTop; // Phim hot/top
        public boolean askNew; // Phim mới
        public boolean askSubtitle; // Phụ đề
        public boolean askDubbing; // Lồng tiếng
        public boolean ask3D; // Phim 3D

        // public boolean askIMAX; // IMAX

        public boolean askError; // Lỗi kỹ thuật
        public boolean askHelp; // Cần hỗ trợ

        // Kiểm tra có intent nào được bật
        public boolean hasAnyIntent() {
            return genre != null || askPromotion || askDirector || askActor || askDuration || askPrice || askShowTimes
                    ||
                    askLanguage || askAgeLimit || askTrailer || askBooking || askReview || askCinemaInfo || askPayment
                    ||
                    askSeatSelection || askGroupDiscount || askStudentDiscount || askLocation || askContact
                    || askOpenHours ||
                    askComingSoon || askNowShowing || askTop || askNew || askSubtitle || askDubbing || ask3D || askError
                    || askHelp;
        }
    }

    public static QueryInfo analyze(String question) {
        QueryInfo info = new QueryInfo();
        String q = question.toLowerCase();

        // Chào hỏi chung - không cần truy vấn phim
        if (q.matches(".*\\b(xin chào|chào|hello|hi|chào bạn|chào em)\\b.*") ||
                q.matches(".*\\b(cảm ơn|thank you|thanks)\\b.*") ||
                q.matches(".*\\b(tạm biệt|bye|goodbye)\\b.*")) {
            return info; // Trả về info rỗng, không có intent cụ thể
        }

        // Thể loại
        if (q.contains("hoạt hình"))
            info.genre = "Hoạt hình";
        if (q.contains("hành động"))
            info.genre = "Hành động";
        if (q.contains("tình cảm"))
            info.genre = "Tình cảm";
        if (q.contains("kinh dị"))
            info.genre = "Kinh dị";
        if (q.contains("hài"))
            info.genre = "Hài";
        if (q.contains("phiêu lưu"))
            info.genre = "Phiêu lưu";
        if (q.contains("khoa học viễn tưởng") || q.contains("sci-fi"))
            info.genre = "Khoa học viễn tưởng";
        if (q.contains("lịch sử"))
            info.genre = "Lịch sử";
        if (q.contains("chiến tranh"))
            info.genre = "Chiến tranh";
        if (q.contains("âm nhạc"))
            info.genre = "Âm nhạc";
        if (q.contains("gia đình"))
            info.genre = "Gia đình";
        if (q.contains("thể thao"))
            info.genre = "Thể thao";
        if (q.contains("tài liệu"))
            info.genre = "Tài liệu";
        if (q.contains("siêu anh hùng") || q.contains("marvel") || q.contains("dc"))
            info.genre = "khoa học viễn tưởng";
        // ... mở rộng thêm nếu cần

        // Thời gian chi tiết hơn
        if (q.contains("tối nay"))
            info.time = "19:00";
        if (q.contains("sáng mai"))
            info.time = "09:00";
        if (q.contains("chiều nay"))
            info.time = "15:00";
        if (q.contains("trưa nay"))
            info.time = "12:00";
        if (q.contains("tối mai"))
            info.time = "19:00";
        if (q.contains("cuối tuần"))
            info.date = "weekend";
        if (q.contains("thứ 7") || q.contains("thứ bảy"))
            info.date = "saturday";
        if (q.contains("chủ nhật"))
            info.date = "sunday";
        if (q.contains("ngày mai"))
            info.date = "tomorrow";
        if (q.contains("hôm nay"))
            info.date = "today";
        if (q.contains("hôm qua"))
            info.date = "yesterday";
        if (q.contains("tuần sau"))
            info.date = "next_week";
        if (q.contains("tháng sau"))
            info.date = "next_month";
        // ... thêm các rule khác

        // Ngày (dùng regex tìm ngày dạng dd/mm/yyyy hoặc dd-mm-yyyy)
        Pattern datePattern = Pattern.compile("(\\d{1,2}[-/\\.]\\d{1,2}[-/\\.]\\d{2,4})");
        Matcher matcher = datePattern.matcher(q);
        if (matcher.find()) {
            info.date = matcher.group(1);
        }

        // Tên phim (nhiều pattern khác nhau)
        // Pattern 1: "phim [tên phim]"
        Pattern moviePattern1 = Pattern.compile("phim ([a-zA-Z0-9\u00C0-\u1EF9\s:&'\"!.,-]+?)(?:\\s|$|\\?|\\.|,)");
        Matcher movieMatcher1 = moviePattern1.matcher(q);
        if (movieMatcher1.find()) {
            info.movieName = movieMatcher1.group(1).trim();
        }

        // Pattern 2: "[tên phim] có"
        Pattern moviePattern2 = Pattern.compile("([a-zA-Z0-9\u00C0-\u1EF9\s:&'\"!.,-]+?)\\s+có\\s");
        Matcher movieMatcher2 = moviePattern2.matcher(q);
        if (movieMatcher2.find() && info.movieName == null) {
            String candidate = movieMatcher2.group(1).trim();
            // Kiểm tra không phải là các từ thông thường
            if (!candidate.matches(".*(thể loại|rạp|cinema|theater).*")) {
                info.movieName = candidate;
            }
        }

        // Pattern 3: Tên phim trong dấu ngoặc kép
        Pattern moviePattern3 = Pattern.compile("\"([^\"]+)\"");
        Matcher movieMatcher3 = moviePattern3.matcher(q);
        if (movieMatcher3.find() && info.movieName == null) {
            info.movieName = movieMatcher3.group(1).trim();
        }

        // Khuyến mãi
        if (q.contains("khuyến mãi") || q.contains("ưu đãi") || q.contains("giảm giá"))
            info.askPromotion = true;

        // Hỏi về đạo diễn
        if (q.contains("đạo diễn"))
            info.askDirector = true;

        // Hỏi về diễn viên
        if (q.contains("diễn viên"))
            info.askActor = true;

        // Hỏi về thời lượng
        if (q.contains("thời lượng") || q.contains("bao lâu") || q.contains("dài không"))
            info.askDuration = true;

        // Hỏi về giá vé
        if (q.contains("giá vé") || q.contains("bao nhiêu tiền") || q.contains("mắc không") || q.contains("rẻ không"))
            info.askPrice = true;

        // Hỏi về suất chiếu
        if (q.contains("suất chiếu") || q.contains("giờ chiếu") || q.contains("chiếu lúc mấy giờ") ||
                q.contains("lịch chiếu") || q.contains("thời gian chiếu") || q.contains("chiếu vào"))
            info.askShowTimes = true;

        // Hỏi về ngôn ngữ
        if (q.contains("ngôn ngữ") || q.contains("tiếng gì"))
            info.askLanguage = true;

        // Hỏi về giới hạn tuổi
        if (q.contains("giới hạn tuổi") || q.contains("độ tuổi"))
            info.askAgeLimit = true;

        // Hỏi về trailer
        if (q.contains("trailer") || q.contains("xem thử"))
            info.askTrailer = true;

        // Đặt vé
        if (q.contains("đặt vé") || q.contains("book") || q.contains("booking") ||
                q.contains("mua vé") || q.contains("đặt chỗ") || q.contains("reserv"))
            info.askBooking = true;

        // Đánh giá phim
        if (q.contains("đánh giá") || q.contains("review") || q.contains("rating") ||
                q.contains("sao") || q.contains("hay không") || q.contains("có hay"))
            info.askReview = true;

        // Thông tin rạp
        if (q.contains("rạp") || q.contains("cinema") || q.contains("theater") ||
                q.contains("cơ sở vật chất") || q.contains("trang thiết bị"))
            info.askCinemaInfo = true;

        // Đồ ăn/nước uống
        // if (q.contains("bắp rang") || q.contains("nước ngọt") || q.contains("combo")
        // ||
        // q.contains("đồ ăn") || q.contains("thức ăn") || q.contains("nước uống") ||
        // q.contains("popcorn") || q.contains("coca") || q.contains("pepsi"))
        // info.askFood = true;

        // Chỗ đỗ xe
        // if (q.contains("đỗ xe") || q.contains("gửi xe") || q.contains("parking") ||
        // q.contains("chỗ để xe") || q.contains("bãi xe"))
        // info.askParking = true;

        // Phòng VIP
        // if (q.contains("vip") || q.contains("vip room") || q.contains("phòng vip") ||
        // q.contains("ghế vip") || q.contains("premium"))
        // info.askVipRoom = true;

        // Thanh toán
        if (q.contains("thanh toán") || q.contains("payment") || q.contains("ví điện tử") ||
                q.contains("momo") || q.contains("zalopay") || q.contains("thẻ") ||
                q.contains("visa") || q.contains("mastercard") || q.contains("atm"))
            info.askPayment = true;

        // // Hoàn tiền
        // if (q.contains("hoàn tiền") || q.contains("refund") || q.contains("trả lại
        // tiền") ||
        // q.contains("hủy vé") || q.contains("cancel"))
        // info.askRefund = true;

        // Đổi vé/suất chiếu
        if (q.contains("đổi vé") || q.contains("đổi suất") || q.contains("thay đổi suất") || q.contains("thay đổi vé")
                || q.contains("change ticket") || q.contains("change showtime") || q.contains("chuyển suất"))
            info.askChangeTicket = true;

        // Hủy vé/hoàn tiền
        if (q.contains("hủy vé") || q.contains("huỷ vé") || q.contains("hủy đặt vé") || q.contains("huỷ đặt vé")
                || q.contains("hoàn tiền") || q.contains("refund") || q.contains("trả lại vé")
                || q.contains("cancel ticket"))
            info.askRefund = true;

        // Chọn ghế
        if (q.contains("chọn ghế") || q.contains("seat") || q.contains("vị trí ghế") ||
                q.contains("ghế ngồi") || q.contains("hàng ghế"))
            info.askSeatSelection = true;

        // Giảm giá nhóm
        if (q.contains("giảm giá nhóm") || q.contains("group discount") ||
                q.contains("vé nhóm") || q.contains("đi nhóm"))
            info.askGroupDiscount = true;

        // Giảm giá học sinh
        if (q.contains("học sinh") || q.contains("sinh viên") || q.contains("student") ||
                q.contains("giảm giá học sinh") || q.contains("ưu đãi học sinh"))
            info.askStudentDiscount = true;

        // Địa chỉ rạp
        if (q.contains("địa chỉ") || q.contains("ở đâu") || q.contains("location") ||
                q.contains("address") || q.contains("đường") || q.contains("quận"))
            info.askLocation = true;

        // Liên hệ
        if (q.contains("liên hệ") || q.contains("contact") || q.contains("số điện thoại") ||
                q.contains("hotline") || q.contains("email") || q.contains("facebook"))
            info.askContact = true;

        // Giờ mở cửa
        if (q.contains("giờ mở cửa") || q.contains("mở cửa") || q.contains("đóng cửa") ||
                q.contains("open") || q.contains("close") || q.contains("hoạt động"))
            info.askOpenHours = true;

        // Phim sắp chiếu
        if (q.contains("sắp chiếu") || q.contains("coming soon") || q.contains("sắp ra") ||
                q.contains("tương lai") || q.contains("tuần sau") || q.contains("tháng sau"))
            info.askComingSoon = true;

        // Phim đang chiếu
        if (q.contains("đang chiếu") || q.contains("now showing") || q.contains("hiện tại") ||
                q.contains("bây giờ") || q.contains("tuần này"))
            info.askNowShowing = true;

        // Phim hot/top
        if (q.contains("hot") || q.contains("top") || q.contains("nổi tiếng") ||
                q.contains("phổ biến") || q.contains("trending") || q.contains("bom tấn"))
            info.askTop = true;

        // Phim mới
        if (q.contains("phim mới") || q.contains("new") || q.contains("ra mắt") ||
                q.contains("vừa ra") || q.contains("mới nhất"))
            info.askNew = true;

        // Phụ đề
        if (q.contains("phụ đề") || q.contains("subtitle") || q.contains("sub") ||
                q.contains("tiếng việt") || q.contains("phụ đề việt"))
            info.askSubtitle = true;

        // Lồng tiếng
        if (q.contains("lồng tiếng") || q.contains("dubbed") || q.contains("voice over") ||
                q.contains("thuyết minh"))
            info.askDubbing = true;

        // Phim 3D
        if (q.contains("3d") || q.contains("ba chiều") || q.contains("three d"))
            info.ask3D = true;

        // IMAX
        // if (q.contains("imax") || q.contains("màn hình lớn"))
        // info.askIMAX = true;

        // Lỗi kỹ thuật
        if (q.contains("lỗi") || q.contains("error") || q.contains("không hoạt động") ||
                q.contains("bị lỗi") || q.contains("không được") || q.contains("sự cố"))
            info.askError = true;

        // Cần hỗ trợ
        if (q.contains("hỗ trợ") || q.contains("help") || q.contains("giúp đỡ") ||
                q.contains("trợ giúp") || q.contains("support") || q.contains("assistance"))
            info.askHelp = true;

        // Nhận diện giờ cụ thể (HH:mm hoặc HHh)
        Pattern timePattern = Pattern.compile("(\\d{1,2})[:h]\\s*(\\d{0,2})");
        Matcher timeMatcher = timePattern.matcher(q);
        if (timeMatcher.find()) {
            String hour = timeMatcher.group(1);
            String minute = timeMatcher.group(2);
            if (minute.isEmpty())
                minute = "00";
            info.time = String.format("%02d:%02s", Integer.parseInt(hour), minute);
        }

        return info;
    }
}
