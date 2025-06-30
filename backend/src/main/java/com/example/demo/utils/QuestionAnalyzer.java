package com.example.demo.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class QuestionAnalyzer {
    public static class QueryInfo {
        public String genre;
        public String time;
        public String date;
        public String movieName;
        public boolean askPromotion;
        public boolean askDirector;
        public boolean askActor;
        public boolean askDuration;
        public boolean askPrice;
        public boolean askShowTimes;
        public boolean askLanguage;
        public boolean askAgeLimit;
        public boolean askTrailer;
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

        // Thời gian
        if (q.contains("tối nay"))
            info.time = "19:00";
        if (q.contains("sáng mai"))
            info.time = "09:00";
        if (q.contains("chiều nay"))
            info.time = "15:00";
        if (q.contains("ngày mai"))
            info.date = "tomorrow";
        if (q.contains("hôm nay"))
            info.date = "today";
        // ... thêm các rule khác

        // Ngày (dùng regex tìm ngày dạng dd/mm/yyyy hoặc dd-mm-yyyy)
        Pattern datePattern = Pattern.compile("(\\d{1,2}[-/\\.]\\d{1,2}[-/\\.]\\d{2,4})");
        Matcher matcher = datePattern.matcher(q);
        if (matcher.find()) {
            info.date = matcher.group(1);
        }

        // Tên phim (giả lập: nếu có từ "phim" và sau đó là tên)
        Pattern moviePattern = Pattern.compile("phim ([a-zA-Z0-9\u00C0-\u1EF9\s]+)");
        Matcher movieMatcher = moviePattern.matcher(q);
        if (movieMatcher.find()) {
            info.movieName = movieMatcher.group(1).trim();
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
        if (q.contains("suất chiếu") || q.contains("giờ chiếu") || q.contains("chiếu lúc mấy giờ"))
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

        return info;
    }
}
