# Test Account-Related Intents for MovieTheater Chatbot

## Test Cases for New Account Features

### 1. Forgot Password Intent

**Test inputs:**

-   "Tôi quên mật khẩu"
-   "Quên password"
-   "Làm sao để lấy lại mật khẩu?"
-   "Không nhớ mật khẩu"
-   "Reset password"

**Expected Response:**

```
HƯỚNG DẪN LẤY LẠI MẬT KHẨU:
1. Truy cập trang đăng nhập của MovieTheater
2. Nhấp vào liên kết 'Quên mật khẩu?'
3. Nhập email đã đăng ký tài khoản
4. Kiểm tra email và làm theo hướng dẫn để đặt lại mật khẩu
5. Đăng nhập bằng mật khẩu mới

Nếu gặp khó khăn, vui lòng liên hệ hotline: 1900-xxxx để được hỗ trợ.
```

### 2. Account Registration Intent

**Test inputs:**

-   "Tôi muốn đăng ký tài khoản"
-   "Làm sao để tạo tài khoản?"
-   "Đăng kí thành viên"
-   "Sign up"
-   "Tài khoản mới"

**Expected Response:**

```
HƯỚNG DẪN ĐĂNG KÝ TÀI KHOẢN MOVIETHEATER:
1. Truy cập website www.movietheater.com
2. Nhấp vào nút 'Đăng ký' ở góc phải màn hình
3. Điền đầy đủ thông tin:
   - Họ và tên
   - Email (dùng làm tên đăng nhập)
   - Số điện thoại
   - Mật khẩu (tối thiểu 6 ký tự)
   - Ngày sinh
4. Xác nhận điều khoản sử dụng
5. Nhấn 'Đăng ký' để hoàn tất
6. Kiểm tra email để xác thực tài khoản

Lưu ý: Tài khoản thành viên sẽ được tích điểm và nhận nhiều ưu đãi hấp dẫn!
```

### 3. Profile Update Intent

**Test inputs:**

-   "Cập nhật thông tin cá nhân"
-   "Đổi thông tin hồ sơ"
-   "Chỉnh sửa profile"
-   "Thay đổi email"
-   "Đổi số điện thoại"
-   "Update profile"

**Expected Response:**

```
HƯỚNG DẪN CẬP NHẬT THÔNG TIN CÁ NHÂN:
1. Đăng nhập vào tài khoản MovieTheater
2. Nhấp vào avatar/tên của bạn ở góc phải màn hình
3. Chọn 'Thông tin cá nhân' hoặc 'Hồ sơ'
4. Chỉnh sửa các thông tin cần thiết:
   - Họ tên, số điện thoại, email
   - Ngày sinh, giới tính
   - Địa chỉ
   - Ảnh đại diện
5. Nhấn 'Lưu thay đổi' để cập nhật

Lưu ý: Một số thông tin như email có thể cần xác thực lại qua email mới.
```

### 4. Ticket Change/Cancel Intent (Previously implemented)

**Test inputs:**

-   "Tôi muốn hủy vé"
-   "Đổi vé"
-   "Hoàn tiền"

**Expected Response:**

```
Theo quy định của MovieTheater, sau khi đặt vé thành công, khách hàng KHÔNG thể đổi suất chiếu hoặc hủy/hoàn vé. Tuy nhiên, bạn có thể chuyển vé cho người khác sử dụng.
```

## Testing Instructions

1. **Start the backend server:**

    ```bash
    cd backend
    ./mvnw spring-boot:run
    ```

2. **Start the frontend:**

    ```bash
    cd frontend
    npm start
    ```

3. **Test via chatbot interface:**

    - Open the frontend application
    - Click on the chatbot icon
    - Try each test input and verify the responses
    - Test both typing manually and using the new quick reply buttons

4. **Test via API directly:**
    ```bash
    curl -X POST http://localhost:8081/api/public/chatbot \
    -H "Content-Type: application/json" \
    -d '{
      "model": "vistral-7b-chat",
      "messages": [{"role": "user", "content": "Tôi quên mật khẩu"}],
      "stream": false
    }'
    ```

## Success Criteria

✅ All account-related intents are correctly recognized
✅ Responses are returned immediately without AI processing
✅ Quick reply buttons are available in the frontend
✅ Both streaming and non-streaming endpoints work correctly
✅ Responses are clear, helpful, and actionable
✅ No compilation or runtime errors

## Notes

-   These responses are returned directly from the backend without LLM processing for consistency and reliability
-   The intent recognition supports both Vietnamese and English keywords
-   Quick reply buttons make it easy for users to access common account functions
-   The system maintains the existing functionality for movie-related queries while adding new account management capabilities
