CREATE TABLE ROLES (
    ROLE_ID SERIAL PRIMARY KEY, -- Khóa chính tự tăng cho mỗi vai trò.
    ROLE_NAME VARCHAR(255) UNIQUE -- Tên của vai trò, phải là duy nhất.
);

-- Bảng ACCOUNT: Lưu trữ thông tin chi tiết về tài khoản của người dùng.
CREATE TABLE ACCOUNT (
    ACCOUNT_ID SERIAL PRIMARY KEY, -- Khóa chính tự tăng cho mỗi tài khoản.
    ROLE_ID INT REFERENCES ROLES(ROLE_ID), -- Khóa ngoại, liên kết đến vai trò của người dùng trong bảng ROLES.
    EMAIL VARCHAR(255) UNIQUE,             -- Email của người dùng, phải là duy nhất trong hệ thống.
    PASSWORD VARCHAR(255),                 -- Mật khẩu của người dùng (nên được lưu dưới dạng băm).
    FULL_NAME VARCHAR(255),                -- Họ và tên đầy đủ của người dùng.
    GENDER VARCHAR(255),                   -- Giới tính của người dùng.
    PHONE_NUMBER VARCHAR(50) UNIQUE,       -- Số điện thoại, phải là duy nhất.
    IDENTITY_CARD VARCHAR(50) UNIQUE,      -- Số CMND/CCCD, phải là duy nhất.
    DATE_OF_BIRTH DATE,                    -- Ngày sinh của người dùng.
    REGISTER_DATE DATE,                    -- Ngày đăng ký tài khoản.
    SCORE INT,                             -- Điểm tích lũy của người dùng.
    AVATAR VARCHAR(255),                   -- Đường dẫn đến ảnh đại diện.
    ACCOUNT_STATUS INT,            -- Trạng thái của tài khoản (ví dụ: 'active', 'inactive').
    SOCIAL_ACCOUNT_TYPE VARCHAR(50)        -- Loại tài khoản mạng xã hội nếu đăng nhập qua Google, Facebook...
);

-- Bảng EMAIL_VERIFICATION_TOKEN: Lưu trữ mã token dùng để xác thực email.
CREATE TABLE EMAIL_VERIFICATION_TOKENS (
    EMAIL_VERIFICATION_TOKEN_ID SERIAL PRIMARY KEY, -- Khóa chính tự tăng.
    EMAIL VARCHAR(255),                     -- Email cần xác thực.
    OTP_CODE VARCHAR(255),                  -- Mã OTP hoặc token được gửi đến email.
    EXPIRATION_TIME TIMESTAMP WITHOUT TIME ZONE -- Thời gian hết hạn của token.
);

-- Bảng PAYMENT_METHOD: Lưu trữ các phương thức thanh toán được chấp nhận.
CREATE TABLE PAYMENT_METHOD (
    PAYMENT_METHOD_ID SERIAL PRIMARY KEY, -- Khóa chính tự tăng.
    METHOD_NAME VARCHAR(100) UNIQUE,       -- Tên phương thức (ví dụ: 'Credit Card', 'Momo'), phải là duy nhất.
    DESCRIPTION VARCHAR(255),                -- Mô tả chi tiết về phương thức.
    IS_ACTIVE BOOLEAN                        -- Trạng thái cho biết phương thức có đang được sử dụng hay không.
);

-- Bảng PROMOTION: Chứa thông tin về các chương trình khuyến mãi, mã giảm giá.
CREATE TABLE PROMOTION (
    PROMOTION_ID SERIAL PRIMARY KEY,       -- Khóa chính tự tăng.
    DISCOUNT_TYPE VARCHAR(50),             -- Loại giảm giá (ví dụ: 'percentage', 'fixed_amount').
    DISCOUNT_LEVEL DECIMAL(10,2),          -- Mức độ giảm giá.
    CODE VARCHAR(255) UNIQUE,              -- Mã khuyến mãi, phải là duy nhất.
    DETAIL VARCHAR(255),                   -- Chi tiết về chương trình khuyến mãi.
    MAX_DISCOUNT DECIMAL(12,2),            -- Mức giảm giá tối đa có thể áp dụng.
    MIN_ORDER DECIMAL(12,2),               -- Giá trị đơn hàng tối thiểu để áp dụng.
    START_TIME TIMESTAMP WITHOUT TIME ZONE,  -- Thời gian bắt đầu khuyến mãi.
    END_TIME TIMESTAMP WITHOUT TIME ZONE,    -- Thời gian kết thúc khuyến mãi.
    PROMOTION_ACTIVE BOOLEAN,              -- Trạng thái cho biết khuyến mãi có đang hoạt động hay không.
    IMAGE VARCHAR(255)                     -- Đường dẫn đến hình ảnh quảng cáo cho khuyến mãi.
);

-- Bảng TYPE: Lưu trữ các thể loại phim.
CREATE TABLE TYPE (
    TYPE_ID SERIAL PRIMARY KEY,           -- Khóa chính tự tăng cho mỗi thể loại.
    TYPE_NAME VARCHAR(255) UNIQUE          -- Tên thể loại (ví dụ: 'Hành động', 'Hài'), phải là duy nhất.
);

-- Bảng MOVIE: Chứa tất cả thông tin chi tiết về một bộ phim.
CREATE TABLE MOVIE (
    MOVIE_ID SERIAL PRIMARY KEY,           -- Khóa chính tự tăng cho mỗi phim.
    MOVIE_NAME_VN VARCHAR(255),            -- Tên phim bằng tiếng Việt.
    MOVIE_NAME_EN VARCHAR(255),            -- Tên phim bằng tiếng Anh.
    DURATION INT,                          -- Thời lượng phim (tính bằng phút).
    CONTENT TEXT,                          -- Tóm tắt nội dung phim.
    DIRECTOR VARCHAR(255),                 -- Tên đạo diễn.
    ACTOR VARCHAR(255),                    -- Danh sách diễn viên.
    MOVIE_PRODUCTION_COMPANY VARCHAR(255), -- Công ty sản xuất phim.
    FROM_DATE DATE,                        -- Ngày bắt đầu chiếu.
    TO_DATE DATE,                          -- Ngày kết thúc chiếu.
    SMALL_IMAGE VARCHAR(255),              -- Đường dẫn đến ảnh poster nhỏ.
    LARGE_IMAGE VARCHAR(255),              -- Đường dẫn đến ảnh poster lớn.
    TRAILER VARCHAR(255)                   -- Đường dẫn đến trailer phim.
);

-- Bảng MOVIE_TYPE: Bảng nối, tạo mối quan hệ nhiều-nhiều giữa MOVIE và TYPE.
-- Một phim có thể thuộc nhiều thể loại, và một thể loại có thể có nhiều phim.
CREATE TABLE MOVIE_TYPE (
    MOVIE_TYPE_ID SERIAL PRIMARY KEY,
    MOVIE_ID INT REFERENCES MOVIE(MOVIE_ID) ON DELETE CASCADE, -- Liên kết đến phim.
    TYPE_ID INT REFERENCES TYPE(TYPE_ID) ON DELETE CASCADE,    -- Liên kết đến thể loại.
    UNIQUE (MOVIE_ID, TYPE_ID) -- Đảm bảo một phim không bị gán cùng một thể loại nhiều lần.
);

-- Bảng CINEMA_ROOM: Lưu trữ thông tin về các phòng chiếu phim.
CREATE TABLE CINEMA_ROOM (
    CINEMA_ROOM_ID SERIAL PRIMARY KEY,   -- Khóa chính tự tăng cho mỗi phòng chiếu.
    CINEMA_ROOM_NAME VARCHAR(255),         -- Tên phòng chiếu (ví dụ: 'Phòng 1', 'Phòng IMAX').
    SEAT_QUANTITY INT CHECK (SEAT_QUANTITY > 0 OR SEAT_QUANTITY IS NULL) -- Tổng số ghế trong phòng, phải là số dương.
);

-- Bảng FARE_TYPE: Định nghĩa các loại giá vé khác nhau.
CREATE TABLE FARE_TYPE (
    FARE_TYPE_ID SERIAL PRIMARY KEY,     -- Khóa chính tự tăng.
    FARE_TYPE_NAME VARCHAR(255) UNIQUE,    -- Tên loại giá vé (ví dụ: 'Người lớn', 'Trẻ em', 'VIP'), phải là duy nhất.
    PRICE DECIMAL(10,2) CHECK (PRICE >= 0 OR PRICE IS NULL) -- Giá vé tương ứng, không được âm.
);

-- Bảng SCREENING: Đại diện cho một suất chiếu cụ thể của một bộ phim.
CREATE TABLE SCREENING (
    SCREENING_ID SERIAL PRIMARY KEY,   -- Khóa chính tự tăng cho mỗi suất chiếu.
    MOVIE_ID INT REFERENCES MOVIE(MOVIE_ID) ON DELETE RESTRICT, -- Liên kết đến phim được chiếu.
    CINEMA_ROOM_ID INT REFERENCES CINEMA_ROOM(CINEMA_ROOM_ID) ON DELETE RESTRICT, -- Liên kết đến phòng chiếu.
    FARE_TYPE_ID INT REFERENCES FARE_TYPE(FARE_TYPE_ID) ON DELETE RESTRICT,     -- Liên kết đến loại giá vé cơ bản cho suất chiếu này.
    SHOW_DATE_TIME TIMESTAMP WITHOUT TIME ZONE,                                 -- Thời gian bắt đầu suất chiếu.
    UNIQUE (CINEMA_ROOM_ID, SHOW_DATE_TIME) -- Đảm bảo tại một phòng chiếu, một thời điểm chỉ có một suất chiếu.
);

-- Bảng SEAT: Định nghĩa thông tin của từng ghế trong một phòng chiếu.
CREATE TABLE SEAT (
    SEAT_ID SERIAL PRIMARY KEY,         -- Khóa chính tự tăng cho mỗi ghế.
    CINEMA_ROOM_ID INT REFERENCES CINEMA_ROOM(CINEMA_ROOM_ID) ON DELETE CASCADE, -- Liên kết đến phòng chiếu chứa ghế này.
    SEAT_COL VARCHAR(10),                   -- Vị trí cột của ghế (ví dụ: '1', '2').
    SEAT_ROW VARCHAR(10),                   -- Vị trí hàng của ghế (ví dụ: 'A', 'B').
    SEAT_STATUS INTEGER,                -- Trạng thái của ghế (ví dụ: 'available 1', 'booked 0').
    SEAT_TYPE INTEGER,                 -- Loại ghế (0 Regular, 1 VIP , 2 COUPLE)
    UNIQUE (CINEMA_ROOM_ID, SEAT_ROW, SEAT_COL) -- Đảm bảo vị trí mỗi ghế là duy nhất trong một phòng chiếu.
);

-- Bảng BOOKING: Lưu trữ thông tin về một đơn đặt vé của khách hàng.
CREATE TABLE BOOKING (
    BOOKING_ID SERIAL PRIMARY KEY,     -- Khóa chính tự tăng cho mỗi đơn đặt vé.
    ACCOUNT_ID INT REFERENCES ACCOUNT(ACCOUNT_ID),       -- Liên kết đến tài khoản đã đặt vé. Có thể NULL cho khách vãng lai.
    SCREENING_ID INT REFERENCES SCREENING(SCREENING_ID), -- Liên kết đến suất chiếu được đặt.
    PROMOTION_ID INT REFERENCES PROMOTION(PROMOTION_ID), -- Liên kết đến khuyến mãi đã được áp dụng (nếu có).
    PROMOTION_CODE_APPLIED VARCHAR(255),   -- Mã khuyến mãi thực tế đã được sử dụng.
    DISCOUNT_TYPE_APPLIED VARCHAR(50),     -- Loại giảm giá đã áp dụng.
    DISCOUNT_APPLIED DECIMAL(12,2),        -- Số tiền hoặc phần trăm được giảm.
    BOOKING_TIME TIMESTAMP WITHOUT TIME ZONE, -- Thời gian thực hiện đặt vé.
    TOTAL_AMOUNT DECIMAL(12,2), -- Tổng số tiền của đơn đặt vé.
    BOOKING_STATUS VARCHAR(50)             -- Trạng thái của đơn đặt vé (ví dụ: 'pending', 'confirmed', 'cancelled').
);

-- Bảng BOOKED_SEAT: Bảng nối, cho biết những ghế nào đã được đặt trong một đơn đặt vé cụ thể.
CREATE TABLE BOOKED_SEAT (
    BOOKED_SEAT_ID SERIAL PRIMARY KEY,
    BOOKING_ID INT REFERENCES BOOKING(BOOKING_ID) ON DELETE CASCADE, -- Liên kết đến đơn đặt vé.
    SEAT_ID INT REFERENCES SEAT(SEAT_ID) ON DELETE RESTRICT,         -- Liên kết đến ghế đã được chọn.
    PRICE_PAID DECIMAL(12,2),                                        -- Giá thực trả cho chiếc ghế này.
    UNIQUE (BOOKING_ID, SEAT_ID) -- Đảm bảo một ghế không thể xuất hiện hai lần trong cùng một đơn đặt vé.
);

-- Bảng PAYMENT: Ghi lại thông tin giao dịch thanh toán cho một đơn đặt vé.
CREATE TABLE PAYMENT (
    PAYMENT_ID SERIAL PRIMARY KEY,     -- Khóa chính tự tăng cho mỗi giao dịch.
    BOOKING_ID INT REFERENCES BOOKING(BOOKING_ID) UNIQUE, -- Liên kết đến đơn đặt vé. UNIQUE đảm bảo một đơn đặt vé chỉ có một thanh toán.
    PAYMENT_METHOD_ID INT REFERENCES PAYMENT_METHOD(PAYMENT_METHOD_ID), -- Liên kết đến phương thức thanh toán đã sử dụng.
    AMOUNT_PAID DECIMAL(12,2),                                          -- Số tiền đã thanh toán.
    TRANSACTION_DATE TIMESTAMP WITHOUT TIME ZONE,                       -- Thời gian giao dịch.
    TRANSACTION_ID_PROVIDER VARCHAR(255),                               -- Mã giao dịch từ nhà cung cấp dịch vụ thanh toán (ví dụ: Momo, VNPay).
    PAYMENT_STATUS VARCHAR(50),                                         -- Trạng thái thanh toán (ví dụ: 'successful', 'failed').
    NOTES TEXT                                                          -- Ghi chú thêm về giao dịch.
);

-- Bảng REVIEW: Lưu trữ các đánh giá của người dùng về phim.
CREATE TABLE REVIEW (
    REVIEW_ID SERIAL PRIMARY KEY,      -- Khóa chính tự tăng cho mỗi đánh giá.
    MOVIE_ID INT REFERENCES MOVIE(MOVIE_ID) ON DELETE CASCADE,       -- Liên kết đến phim được đánh giá.
    ACCOUNT_ID INT REFERENCES ACCOUNT(ACCOUNT_ID) ON DELETE CASCADE, -- Liên kết đến tài khoản đã viết đánh giá.
    RATING INT, -- Điểm đánh giá (ví dụ: từ 1 đến 5 sao).
    COMMENT TEXT,                                                    -- Nội dung bình luận.
    REVIEW_DATE TIMESTAMP WITHOUT TIME ZONE,                         -- Ngày gửi đánh giá.
    IS_APPROVED BOOLEAN,                                             -- Trạng thái cho biết đánh giá đã được duyệt hay chưa.
    SPOILER_ALERT BOOLEAN,                                           -- Cảnh báo nếu đánh giá có tiết lộ nội dung phim.
    UNIQUE (MOVIE_ID, ACCOUNT_ID) -- Đảm bảo một người dùng chỉ có thể đánh giá một bộ phim một lần.
);



-- Dữ liệu cho bảng ROLES
INSERT INTO ROLES (ROLE_NAME) VALUES
('Admin'),       -- ROLE_ID sẽ là 1
('User');        -- ROLE_ID sẽ là 2

-- Dữ liệu cho bảng ACCOUNT
-- Giả sử ROLE_ID 1 là Admin, 2 là User
-- ACCOUNT_STATUS: 1 = Active, 0 = Pending
INSERT INTO ACCOUNT (ROLE_ID, EMAIL, PASSWORD, FULL_NAME, GENDER, PHONE_NUMBER, IDENTITY_CARD, DATE_OF_BIRTH, REGISTER_DATE, SCORE, AVATAR, ACCOUNT_STATUS, SOCIAL_ACCOUNT_TYPE) VALUES
(1, 'admin@example.com', 'securepassword123', 'Quản Trị Viên', 'Khác', '0900000000', '001090000000', '1990-01-01', '2023-01-01', 1000, '/avatars/admin.png', 1, NULL),
(2, 'user1@example.com', 'userpass1', 'Nguyễn Văn A', 'Nam', '0912345678', '001200000001', '1995-05-15', '2023-02-10', 150, '/avatars/user1.jpg', 1, NULL),
(2, 'user2@example.com', 'userpass2', 'Trần Thị B', 'Nữ', '0987654321', '001200000002', '1998-11-20', '2023-03-05', 50, '/avatars/user2.png', 0, 'google');


-- Dữ liệu cho bảng PAYMENT_METHOD
INSERT INTO PAYMENT_METHOD (METHOD_NAME, DESCRIPTION, IS_ACTIVE) VALUES
('Thẻ tín dụng/Ghi nợ', 'Thanh toán qua thẻ Visa, Mastercard, JCB', TRUE), 
('Tiền mặt tại quầy', 'Thanh toán trực tiếp tại rạp', TRUE);            

-- Dữ liệu cho bảng PROMOTION
INSERT INTO PROMOTION (DISCOUNT_TYPE, DISCOUNT_LEVEL, CODE, DETAIL, MAX_DISCOUNT, MIN_ORDER, START_TIME, END_TIME, PROMOTION_ACTIVE, IMAGE) VALUES
('percentage', 20.00, 'WELCOME20', 'Giảm 20% cho đơn hàng đầu tiên', 50000.00, 100000.00, '2024-01-01 00:00:00', '2024-12-31 23:59:59', TRUE, '/promo/welcome.png'), -- PROMO_ID = 1
('fixed_amount', 30000.00, 'WEEKEND30K', 'Giảm 30k cho vé cuối tuần', 30000.00, 150000.00, '2024-05-17 00:00:00', '2024-07-31 23:59:59', TRUE, '/promo/weekend.jpg'); -- PROMO_ID = 2

-- Dữ liệu cho bảng TYPE
INSERT INTO TYPE (TYPE_NAME) VALUES
('Hành động'),      -- TYPE_ID = 1
('Phiêu lưu'),     -- TYPE_ID = 2
('Hoạt hình'),     -- TYPE_ID = 3
('Hài'),           -- TYPE_ID = 4
('Kinh dị'),       -- TYPE_ID = 5
('Khoa học viễn tưởng'); -- TYPE_ID = 6

-- Dữ liệu cho bảng MOVIE
INSERT INTO MOVIE (MOVIE_NAME_VN, MOVIE_NAME_EN, DURATION, CONTENT, DIRECTOR, ACTOR, MOVIE_PRODUCTION_COMPANY, FROM_DATE, TO_DATE, SMALL_IMAGE, LARGE_IMAGE, TRAILER) VALUES
('Biệt Đội Siêu Anh Hùng: Hội Tụ', 'The Avengers', 143, 'Nick Fury tập hợp một đội gồm những siêu anh hùng mạnh nhất Trái Đất để thành lập Biệt đội Avengers, nhằm ngăn chặn em trai nuôi của Thor là Loki khuất phục loài người.', 'Joss Whedon', 'Robert Downey Jr., Chris Evans, Scarlett Johansson', 'Marvel Studios', '2012-04-27', '2012-07-27', '/movies/avengers_small.jpg', '/movies/avengers_large.jpg', 'https://youtube.com/avengers_trailer'), -- MOVIE_ID = 1
('Vua Sư Tử', 'The Lion King', 88, 'Hành trình của chú sư tử con Simba để trở thành vị vua của Vùng đất Niềm tự hào.', 'Roger Allers, Rob Minkoff', 'Matthew Broderick, James Earl Jones, Jeremy Irons', 'Walt Disney Pictures', '1994-06-15', '1995-01-15', '/movies/lionking_small.jpg', '/movies/lionking_large.jpg', 'https://youtube.com/lionking_trailer'), -- MOVIE_ID = 2
('Kẻ Cắp Mặt Trăng 3', 'Despicable Me 3', 90, 'Gru gặp lại người anh em song sinh thất lạc Dru và cùng nhau thực hiện một phi vụ trộm cắp.', 'Pierre Coffin, Kyle Balda', 'Steve Carell, Kristen Wiig, Trey Parker', 'Illumination Entertainment', '2017-06-30', '2017-09-30', '/movies/dm3_small.jpg', '/movies/dm3_large.jpg', 'https://youtube.com/dm3_trailer'); -- MOVIE_ID = 3

-- Dữ liệu cho bảng MOVIE_TYPE
-- Giả sử MOVIE_ID 1 là Avengers, 2 là Lion King, 3 là Despicable Me 3
-- TYPE_ID 1=Hành động, 2=Phiêu lưu, 3=Hoạt hình, 4=Hài
INSERT INTO MOVIE_TYPE (MOVIE_ID, TYPE_ID) VALUES
(1, 1), -- Avengers - Hành động
(1, 2), -- Avengers - Phiêu lưu
(1, 6), -- Avengers - Khoa học viễn tưởng
(2, 2), -- Lion King - Phiêu lưu
(2, 3), -- Lion King - Hoạt hình
(3, 3), -- Despicable Me 3 - Hoạt hình
(3, 4); -- Despicable Me 3 - Hài

-- Dữ liệu cho bảng CINEMA_ROOM
INSERT INTO CINEMA_ROOM (CINEMA_ROOM_NAME, SEAT_QUANTITY) VALUES
('Phòng Chiếu 1', 120), -- ROOM_ID = 1
('Phòng Chiếu 2 (VIP)', 80),  -- ROOM_ID = 2
('Phòng Chiếu 3D', 100); -- ROOM_ID = 3

-- Dữ liệu cho bảng FARE_TYPE
INSERT INTO FARE_TYPE (FARE_TYPE_NAME, PRICE) VALUES
('Người lớn', 90000.00),      -- FARE_ID = 1
('Trẻ em (dưới 1m3)', 60000.00), -- FARE_ID = 2
('VIP', 150000.00);           -- FARE_ID = 3



-- Dữ liệu cho bảng SEAT (Một vài ví dụ cho Phòng Chiếu 1 - ROOM_ID = 1)
-- SEAT_STATUS: 'available', 'booked', 'unavailable'
-- SEAT_TYPE: 'regular', 'vip', 'couple'
INSERT INTO SEAT (CINEMA_ROOM_ID, SEAT_COL, SEAT_ROW, SEAT_STATUS, SEAT_TYPE) VALUES
(1, 'A', '1', 'available', 'regular'),  -- SEAT_ID = 1
(1, 'A', '2', 'available', 'regular'),  -- SEAT_ID = 2
(1, 'B', '1', 'available', 'regular'),  -- SEAT_ID = 3
(1, 'B', '2', 'booked', 'regular'),   -- SEAT_ID = 4 (Ghế này đã được đặt)
(1, 'H', '5', 'available', 'vip'),    -- SEAT_ID = 5 (Ghế VIP)
(1, 'H', '6', 'available', 'vip');    -- SEAT_ID = 6



