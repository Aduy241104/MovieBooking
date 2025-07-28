package com.example.demo.controller;

import com.example.demo.DTO.request.ScreeningRequest;
import com.example.demo.DTO.response.ApiResponse;
import com.example.demo.model.Screening;
import com.example.demo.service.ScreeningService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "*") // Cho phép truy cập API từ mọi nguồn (domain)
@RestController // Đánh dấu đây là một REST Controller
@RequestMapping("/api/movieSchedule/admin") // Gán URL gốc cho các endpoint trong controller này
@Slf4j // Tự động tạo logger
public class ShowTimeController {

    @Autowired
    private ScreeningService screeningService; // Inject service xử lý logic liên quan lịch chiếu

    // API thêm mới lịch chiếu
    @PostMapping("/add-time")
    public ApiResponse<Screening> addScreening(@RequestBody ScreeningRequest request) {
        try {
            Screening screening = screeningService.addScreening(request); // Gọi service thêm lịch chiếu
            return ApiResponse.<Screening>builder()
                    .status(HttpStatus.CREATED.value()) // Trả về mã 201 Created
                    .message("Thêm lịch chiếu thành công")
                    .result(screening)
                    .build();
        } catch (IllegalArgumentException e) { // Bắt lỗi nếu request không hợp lệ
            return ApiResponse.<Screening>builder()
                    .status(HttpStatus.BAD_REQUEST.value()) // Mã lỗi 400
                    .message(e.getMessage())
                    .build();
        }
    }

    // API cập nhật lịch chiếu theo ID
    @PutMapping("/update-time/{id}")
    public ApiResponse<Screening> updateScreening(@PathVariable Long id, @RequestBody ScreeningRequest request) {
        try {
            Screening screening = screeningService.updateScreening(id, request); // Gọi service cập nhật
            return ApiResponse.<Screening>builder()
                    .status(HttpStatus.OK.value()) // Mã 200 OK
                    .message("Cập nhật lịch chiếu thành công")
                    .result(screening)
                    .build();
        } catch (IllegalArgumentException e) { // Nếu không tìm thấy hoặc dữ liệu không hợp lệ
            return ApiResponse.<Screening>builder()
                    .status(HttpStatus.BAD_REQUEST.value())
                    .message(e.getMessage())
                    .build();
        }
    }

    // API xóa mềm lịch chiếu (chỉ cập nhật trạng thái isDelete)
    @DeleteMapping("/delete-time/{id}")
    public ApiResponse<Void> softDeleteScreening(@PathVariable Long id) {
        try {
            screeningService.softDeleteScreening(id); // Gọi service để xóa mềm
            return ApiResponse.<Void>builder()
                    .status(HttpStatus.OK.value())
                    .message("Xóa lịch chiếu thành công")
                    .build();
        } catch (IllegalArgumentException e) {
            return ApiResponse.<Void>builder()
                    .status(HttpStatus.BAD_REQUEST.value())
                    .message(e.getMessage())
                    .build();
        }
    }

    // API lấy danh sách các lịch chiếu chưa bị xóa mềm (isDelete = false)
    @GetMapping("/list-isdelete")
    public ApiResponse<List<Screening>> getAllActiveScreenings() {
        List<Screening> screenings = screeningService.getAllActiveScreenings(); // Gọi service lấy danh sách
        return ApiResponse.<List<Screening>>builder()
                .status(HttpStatus.OK.value())
                .message("Danh sách lịch chiếu chưa bị xóa mềm")
                .result(screenings)
                .build();
    }
}
