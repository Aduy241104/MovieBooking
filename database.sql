
-- Bảng này lưu thông tin vai trò người dùng (ví dụ: admin, user).
CREATE TABLE ROLE (
    ROLE_ID BIGSERIAL PRIMARY KEY,
    ROLE_NAME VARCHAR(255) 
);

-- Bảng này lưu thông tin các loại phim (ví dụ: hành động, hài, kinh dị).
CREATE TABLE TYPE (
    TYPE_ID BIGSERIAL PRIMARY KEY,
    TYPE_NAME VARCHAR(255),
	IS_DELETED BOOLEAN DEFAULT FALSE --mới thêm
);

-- Bảng này lưu thông tin các loại ghế và giá cơ bản của chúng.
CREATE TABLE SEAT_TYPE (
    SEAT_TYPE_ID BIGSERIAL PRIMARY KEY,
    SEAT_TYPE_NAME VARCHAR(255) ,
    SEAT_TYPE_PRICE DECIMAL(10, 2) 
);

-- Bảng này lưu thông tin phòng chiếu phim.
CREATE TABLE CINEMA_ROOM (
    CINEMA_ROOM_ID BIGSERIAL PRIMARY KEY,
    CINEMA_ROOM_NAME VARCHAR(255) ,
    SEAT_QUANTITY INTEGER,
	IS_DELETED BOOLEAN DEFAULT FALSE --mới thêm
);

-- Bảng này lưu các loại giá vé khác nhau (ví dụ: ngày thường, cuối tuần).
CREATE TABLE FARE_TYPE (
    FARE_TYPE_ID BIGSERIAL PRIMARY KEY,
    FARE_TYPE_NAME VARCHAR(255) ,
    BASE_PRICE DECIMAL(12, 2) ,
    TIME_SLOT_TYPE VARCHAR(255),
    MOVIE_FORMAT VARCHAR(255)
);

-- Bảng này lưu các chương trình khuyến mãi.
CREATE TABLE PROMOTION (
    PROMOTION_ID BIGSERIAL PRIMARY KEY,
    CODE VARCHAR(255),
    DISCOUNT_TYPE VARCHAR(255),
    DISCOUNT_LEVEL DECIMAL,
    DETAIL VARCHAR(255),
    MAX_DISCOUNT DECIMAL,
    MIN_ORDER DECIMAL,
    START_TIME TIMESTAMPTZ,
    END_TIME TIMESTAMPTZ,
    PROMOTION_ACTIVE BOOLEAN DEFAULT TRUE,
    IS_DELETED BOOLEAN DEFAULT FALSE
);

-- Bảng này lưu các phương thức thanh toán.
CREATE TABLE PAYMENT_METHOD (
    PAYMENT_METHOD_ID BIGSERIAL PRIMARY KEY,
    METHOD_NAME VARCHAR(255) ,
    DESCRIPTION VARCHAR(255),
    IS_ACTIVE BOOLEAN DEFAULT TRUE
);

-- Bảng này lưu thông tin tài khoản người dùng.

CREATE TABLE ACCOUNT (
    ACCOUNT_ID BIGSERIAL PRIMARY KEY,
    ROLE_ID BIGINT , 
    EMAIL VARCHAR(255) ,
    PASSWORD VARCHAR(255) ,
    FULL_NAME VARCHAR(255),
    GENDER VARCHAR(255),
    PHONE_NUMBER VARCHAR(20) ,
    IDENTITY_CARD VARCHAR(255) ,
    DATE_OF_BIRTH DATE,
    REGISTER_DATE DATE DEFAULT CURRENT_DATE,
    SCORE INT DEFAULT 0,
    AVATAR VARCHAR(255),
    ACCOUNT_STATUS INT,
    SOCIAL_ACCOUNT_TYPE VARCHAR(50),
    IS_DELETED BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (ROLE_ID) REFERENCES ROLE(ROLE_ID)
);

-- Bảng này lưu token xác thực email.
CREATE TABLE EMAIL_VERIFICATION_TOKENS (
    EMAIL_VERIFICATION_TOKEN_ID BIGSERIAL PRIMARY KEY,
    EMAIL VARCHAR(255),
    OTP_CODE VARCHAR(255),
    EXPIRATION_TIME TIMESTAMP WITHOUT TIME ZONE
);

-- Bảng này lưu thông tin chi tiết về phim.
CREATE TABLE MOVIE (
    MOVIE_ID BIGSERIAL PRIMARY KEY,
    MOVIE_NAME_VN VARCHAR(255),
    MOVIE_NAME_EN VARCHAR(255),
    DURATION INT,
    AGE_LIMIT INT,
    CONTENT VARCHAR,
    DIRECTOR VARCHAR(255),
    ACTOR VARCHAR,
    MOVIE_PRODUCTION_COMPANY VARCHAR(255),
    FROM_DATE DATE,
    TO_DATE DATE,
    SMALL_IMAGE VARCHAR(255),
    LARGE_IMAGE VARCHAR(255),
    TRAILER VARCHAR(255),
	IS_DELETED BOOLEAN DEFAULT FALSE --mới thêm
);

-- Bảng trung gian cho mối quan hệ nhiều-nhiều giữa MOVIE và TYPE.
CREATE TABLE MOVIE_TYPE (
    MOVIE_TYPE_ID BIGSERIAL PRIMARY KEY,
    MOVIE_ID BIGINT , 
    TYPE_ID BIGINT ,
    FOREIGN KEY (MOVIE_ID) REFERENCES MOVIE(MOVIE_ID),
    FOREIGN KEY (TYPE_ID) REFERENCES TYPE(TYPE_ID)

);

-- Bảng này lưu các bài đánh giá phim của người dùng.

CREATE TABLE REVIEW (
    REVIEW_ID BIGSERIAL PRIMARY KEY,
    MOVIE_ID BIGINT,
    ACCOUNT_ID BIGINT , 
    RATING INT,
    COMMENT VARCHAR,
    REVIEW_DATE TIMESTAMP WITHOUT TIME ZONE,
    IS_APPROVED BOOLEAN,
    SPOILER_ALERT BOOLEAN ,
    IS_DELETED BOOLEAN DEFAULT FALSE, 
    FOREIGN KEY (MOVIE_ID) REFERENCES MOVIE(MOVIE_ID),
    FOREIGN KEY (ACCOUNT_ID) REFERENCES ACCOUNT(ACCOUNT_ID)
);

-- Bảng này lưu thông tin từng ghế trong một phòng chiếu.
CREATE TABLE SEAT (
    SEAT_ID BIGSERIAL PRIMARY KEY,
    SEAT_TYPE_ID BIGINT ,
    CINEMA_ROOM_ID BIGINT , 
    SEAT_COL VARCHAR(10) ,
    SEAT_ROW VARCHAR(10) ,
    SEAT_STATUS VARCHAR(50),
    FOREIGN KEY (SEAT_TYPE_ID) REFERENCES SEAT_TYPE(SEAT_TYPE_ID),
    FOREIGN KEY (CINEMA_ROOM_ID) REFERENCES CINEMA_ROOM(CINEMA_ROOM_ID)
);

-- Bảng này lưu thông tin các suất chiếu phim.
CREATE TABLE SCREENING (
    SCREENING_ID BIGSERIAL PRIMARY KEY,
    MOVIE_ID BIGINT ,
    CINEMA_ROOM_ID BIGINT , 
    FARE_TYPE_ID BIGINT , 
    SHOW_DATE_TIME TIMESTAMP ,
	IS_DELETED BOOLEAN DEFAULT FALSE, --mới thêm
    FOREIGN KEY (MOVIE_ID) REFERENCES MOVIE(MOVIE_ID),
    FOREIGN KEY (CINEMA_ROOM_ID) REFERENCES CINEMA_ROOM(CINEMA_ROOM_ID),
    FOREIGN KEY (FARE_TYPE_ID) REFERENCES FARE_TYPE(FARE_TYPE_ID)
);

-- Bảng này lưu thông tin về một lần đặt vé.
CREATE TABLE BOOKING (
    BOOKING_ID BIGSERIAL PRIMARY KEY,
    ACCOUNT_ID BIGINT , 
    SCREENING_ID BIGINT , 
    PROMOTION_ID BIGINT, 
    PAYMENT_METHOD_ID BIGINT , 
    PROMOTION_CODE_APPLIED VARCHAR(255),
    PROMOTION_TYPE_APPLIED VARCHAR(255),
    DISCOUNT_APPLIED DECIMAL(12, 2) DEFAULT 0,
    BOOKING_TIME TIMESTAMP WITHOUT TIME ZONE DEFAULT NOW(),
    TOTAL_AMOUNT DECIMAL(12, 2) ,
    BOOKING_STATUS VARCHAR(20),
	vnp_TxnRef VARCHAR(255),
	booking_code VARCHAR(20) UNIQUE,
	points_used INTEGER DEFAULT 0,
	points_discount DECIMAL(12, 2) DEFAULT 0,
    FOREIGN KEY (ACCOUNT_ID) REFERENCES ACCOUNT(ACCOUNT_ID),
    FOREIGN KEY (SCREENING_ID) REFERENCES SCREENING(SCREENING_ID),
    FOREIGN KEY (PROMOTION_ID) REFERENCES PROMOTION(PROMOTION_ID),
    FOREIGN KEY (PAYMENT_METHOD_ID) REFERENCES PAYMENT_METHOD(PAYMENT_METHOD_ID)
);

-- Bảng này lưu chi tiết các ghế đã được đặt trong một lần booking.
CREATE TABLE BOOKED_SEAT (
    BOOKED_SEAT_ID BIGSERIAL PRIMARY KEY,
    BOOKING_ID BIGINT , 
    SEAT_ID BIGINT ,
    PRICE_PAID DECIMAL(12, 2) ,
    FOREIGN KEY (BOOKING_ID) REFERENCES BOOKING(BOOKING_ID),
    FOREIGN KEY (SEAT_ID) REFERENCES SEAT(SEAT_ID)
);






-- 1. ROLES (Vai trò)
INSERT INTO ROLE (ROLE_NAME) VALUES
('ADMIN'),
('EMPLOYEE'),
('CUSTOMER');

-- 2. TYPE (Thể loại phim)
INSERT INTO TYPE (TYPE_NAME) VALUES
('Hành động'),
('Hài'),
('Kinh dị'),
('Tình cảm - Lãng mạn'),
('Khoa học viễn tưởng'),
('Hoạt hình'),
('Tâm lý'),
('Phiêu lưu');

-- 3. SEAT_TYPE (Loại ghế)
-- GIẢ ĐỊNH: SEAT_TYPE_PRICE là giá phụ thu thêm cho loại ghế đó.
-- Giá vé cuối cùng = Giá suất chiếu (FARE_TYPE) + Giá phụ thu (SEAT_TYPE).
INSERT INTO SEAT_TYPE (SEAT_TYPE_NAME, SEAT_TYPE_PRICE) VALUES
('regular', 0.00),
('vip', 20000.00),
('couple', 50000.00);

-- 4. CINEMA_ROOM (Phòng chiếu)
INSERT INTO CINEMA_ROOM (CINEMA_ROOM_NAME, SEAT_QUANTITY) VALUES
('Phòng chiếu 1', 80),
('Phòng chiếu 2', 100),
('Phòng chiếu IMAX 3', 150),
('Phòng chiếu GOLD CLASS 4', 40);

-- 5. FARE_TYPE (Loại giá vé)
-- GIẢ ĐỊNH: BASE_PRICE là giá vé cơ bản cho ghế thường tại suất chiếu đó.
-- DAY_PRICE có thể là phụ thu cuối tuần/ngày lễ (ở đây tôi không dùng đến, đặt là 0).
INSERT INTO FARE_TYPE (FARE_TYPE_NAME, BASE_PRICE, TIME_SLOT_TYPE, MOVIE_FORMAT) VALUES
('Vietsub', 70000.00, 'Ngày Thường', '2D'),
('Lồng Tiếng', 70000.00, 'Ngày Lễ', '2D'),
('Lồng Tiếng', 70000.00, 'Cuối Tuần', '2D'),
('Lồng Tiếng', 70000.00, 'Cuối Tuần', '3D'),
('Vietsub', 70000.00, 'Ngày Thường', '3D'),
('Vietsub', 70000.00, 'Ngày Lễ', 'IMAX');

-- 6. PROMOTION (Khuyến mãi)
INSERT INTO PROMOTION (CODE, DISCOUNT_TYPE, DISCOUNT_LEVEL, DETAIL, MAX_DISCOUNT, MIN_ORDER, START_TIME, END_TIME, PROMOTION_ACTIVE, IS_DELETED) VALUES
('GIAM10', 'PERCENT', 10, 'Giảm 10% cho đơn hàng từ 150k, giảm tối đa 30k', 30000, 150000, '2024-01-01T00:00:00Z', '2025-12-31T23:59:59Z', TRUE, FALSE),
('GIAM20K', 'AMOUNT', 20000, 'Giảm thẳng 20k cho đơn hàng từ 200k', 20000, 200000, '2024-05-01T00:00:00Z', '2025-06-30T23:59:59Z', TRUE, FALSE),
('WELCOME', 'AMOUNT', 50000, 'Giảm 50k cho thành viên mới (đơn từ 100k)', 50000, 100000, '2024-01-01T00:00:00Z', '2025-12-31T23:59:59Z', TRUE, FALSE);

-- 7. PAYMENT_METHOD (Phương thức thanh toán)
INSERT INTO PAYMENT_METHOD (METHOD_NAME, DESCRIPTION, IS_ACTIVE) VALUES
('Ví MoMo', 'Thanh toán qua ví điện tử MoMo', TRUE),
('ZaloPay', 'Thanh toán qua ví điện tử ZaloPay', TRUE),
('VNPAY', 'Thanh toán qua cổng VNPAY (Visa, Mastercard, JCB)', TRUE);

-- 8. MOVIE (Phim)
INSERT INTO MOVIE (MOVIE_NAME_VN, MOVIE_NAME_EN, DURATION, AGE_LIMIT, CONTENT, DIRECTOR, ACTOR, MOVIE_PRODUCTION_COMPANY, FROM_DATE, TO_DATE, SMALL_IMAGE, LARGE_IMAGE, TRAILER) VALUES
('Lật Mặt 7: Một Điều Ước', 'Face Off 7: One Wish', 138, 13, 'Câu chuyện về bà Hai và 5 người con của mình, mỗi người một hoàn cảnh. Một tai nạn bất ngờ xảy ra, liệu ai sẽ về chăm sóc mẹ?', 'Lý Hải', 'Trương Minh Cường, Đinh Y Nhung, Quách Ngọc Tuyên', 'Ly Hai Production', '2024-04-26', '2024-06-20', 'https://example.com/images/latmat7_small.jpg', 'https://example.com/images/latmat7_large.jpg', 'https://www.youtube.com/watch?v=kS-t2X_e_tY'),
('Doraemon: Nobita và Bản Giao Hưởng Địa Cầu', 'Doraemon the Movie: Nobita''s Earth Symphony', 115, 0, 'Nobita và nhóm bạn sử dụng bảo bối âm nhạc để giải cứu thế giới khỏi một hiểm họa bí ẩn.', 'Imai Kazuaki', 'Doraemon, Nobita, Shizuka, Jaian, Suneo', 'Toho', '2024-05-24', '2024-07-15', 'https://example.com/images/doraemon_small.jpg', 'https://example.com/images/doraemon_large.jpg', 'https://www.youtube.com/watch?v=example_trailer_2'),
('Hành Tinh Khỉ: Vương Quốc Mới', 'Kingdom of the Planet of the Apes', 145, 13, 'Nhiều thế hệ sau triều đại của Caesar, loài khỉ là loài thống trị trong khi con người phải sống trong bóng tối.', 'Wes Ball', 'Owen Teague, Freya Allan, Kevin Durand', '20th Century Studios', '2024-05-10', '2024-06-30', 'https://example.com/images/apes_small.jpg', 'https://example.com/images/apes_large.jpg', 'https://www.youtube.com/watch?v=example_trailer_3');




-- 9. ACCOUNT (Tài khoản)
-- LƯU Ý: Mật khẩu trong thực tế phải được mã hóa (hashed). Ở đây dùng text thường để minh họa.
INSERT INTO ACCOUNT (ROLE_ID, EMAIL, PASSWORD, FULL_NAME, GENDER, PHONE_NUMBER, IDENTITY_CARD, DATE_OF_BIRTH, SCORE, AVATAR, ACCOUNT_STATUS) VALUES
(1, 'admin@mycinema.com', 'admin_password_hashed', 'Quản Trị Viên', 'Khác', '0987654321', '001090123456', '1990-01-01', 0, 'https://example.com/avatars/admin.png', 1),
(2, 'nguyenvana@gmail.com', 'userA_password_hashed', 'Nguyễn Văn A', 'Nam', '0123456789', '001200987654', '2000-10-20', 150, 'https://example.com/avatars/userA.png', 1),
(2, 'tranthib@yahoo.com', 'userB_password_hashed', 'Trần Thị B', 'Nữ', '0912345678', '034199123456', '1999-05-15', 320, 'https://example.com/avatars/userB.png', 1);

-- 10. MOVIE_TYPE (Bảng nối Phim và Thể loại)
-- Lật Mặt 7 (ID 1) là Tình cảm (ID 4) và Tâm lý (ID 7)
INSERT INTO MOVIE_TYPE (MOVIE_ID, TYPE_ID) VALUES
(1, 4), 
(1, 7);
-- Doraemon (ID 2) là Hoạt hình (ID 6) và Phiêu lưu (ID 8)
INSERT INTO MOVIE_TYPE (MOVIE_ID, TYPE_ID) VALUES
(2, 6),
(2, 8);
-- Hành Tinh Khỉ (ID 3) là Hành động (ID 1) và Khoa học viễn tưởng (ID 5)
INSERT INTO MOVIE_TYPE (MOVIE_ID, TYPE_ID) VALUES
(3, 1),
(3, 5);

-- 11. SEAT (Ghế trong phòng chiếu)
-- Tạo vài ghế mẫu cho Phòng chiếu 1 (ID 1), 80 ghế
-- Hàng A, B là ghế VIP (SEAT_TYPE_ID 2)
INSERT INTO SEAT (SEAT_TYPE_ID, CINEMA_ROOM_ID, SEAT_COL, SEAT_ROW, SEAT_STATUS) VALUES
(2, 1, '1', 'A', 'Available'),
(2, 1, '2', 'A', 'Available'),
(2, 1, '3', 'B', 'Available'),
(2, 1, '4', 'B', 'Available');
-- Hàng C, D là ghế thường (SEAT_TYPE_ID 1)
INSERT INTO SEAT (SEAT_TYPE_ID, CINEMA_ROOM_ID, SEAT_COL, SEAT_ROW, SEAT_STATUS) VALUES
(1, 1, '1', 'C', 'Available'),
(1, 1, '2', 'C', 'Available'),
(1, 1, '3', 'D', 'Available'),
(1, 1, '4', 'D', 'Available');
-- Hàng cuối là ghế đôi (SEAT_TYPE_ID 3)
INSERT INTO SEAT (SEAT_TYPE_ID, CINEMA_ROOM_ID, SEAT_COL, SEAT_ROW, SEAT_STATUS) VALUES
(3, 1, '5', 'H', 'Available'), -- Ghế đôi chiếm 2 vị trí, nhưng ở đây ta chỉ lưu 1 record
(3, 1, '6', 'H', 'Available');
-- (Trong thực tế bạn sẽ cần script để tạo đủ 80 ghế cho phòng này)

-- Tạo ghế cho Phòng chiếu 2 (ID 2), 100 ghế
INSERT INTO SEAT (SEAT_TYPE_ID, CINEMA_ROOM_ID, SEAT_COL, SEAT_ROW, SEAT_STATUS) VALUES
(1, 2, '5', 'E', 'Available'),
(1, 2, '6', 'E', 'Available'),
(2, 2, '7', 'F', 'Available'),
(2, 2, '8', 'F', 'Available');

-- 12. SCREENING (Suất chiếu)
INSERT INTO SCREENING (MOVIE_ID, CINEMA_ROOM_ID, FARE_TYPE_ID, SHOW_DATE_TIME) VALUES
-- Lật Mặt 7 (ID 1) tại phòng 1 (ID 1), suất chiếu cuối tuần 2D (FARE_ID 3)
(1, 1, 3, '2025-06-18T19:30:00'),
-- Doraemon (ID 2) tại phòng 2 (ID 2), suất chiếu ngày thường trước 17h (FARE_ID 1)
(2, 2, 1, '2025-06-12T15:00:00'),
-- Hành Tinh Khỉ (ID 3) tại phòng IMAX 3 (ID 3), suất chiếu IMAX cuối tuần (FARE_ID 6)
(3, 3, 6, '2025-06-15T20:00:00'),
-- Thêm một suất chiếu nữa cho Lật Mặt 7
(1, 2, 2, '2025-06-14T21:00:00');


-- 13. REVIEW (Đánh giá phim)
INSERT INTO REVIEW (MOVIE_ID, ACCOUNT_ID, RATING, COMMENT, REVIEW_DATE, IS_APPROVED, SPOILER_ALERT, IS_DELETED) VALUES
(1, 2, 9, 'Phim rất cảm động và ý nghĩa về gia đình. Mọi người nên đi xem!', '2024-05-10T10:00:00', TRUE, FALSE, FALSE),
(3, 3, 8, 'Kỹ xảo mãn nhãn, cốt truyện hấp dẫn. Đoạn kết hơi bất ngờ.', '2024-05-15T22:30:00', TRUE, TRUE, FALSE);


