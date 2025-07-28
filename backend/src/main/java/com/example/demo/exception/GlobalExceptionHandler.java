package com.example.demo.exception;

import com.example.demo.DTO.response.ApiResponse;

import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.*;
import com.example.demo.DTO.response.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;

import org.springframework.web.bind.MethodArgumentNotValidException;

import com.example.demo.exception.booking.*;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {
        @ExceptionHandler(HttpMessageNotReadableException.class)
        public ResponseEntity<ErrorResponse> handleInvalidFormat(HttpMessageNotReadableException ex,
                        HttpServletRequest request) {
                ErrorResponse errorResponse = ErrorResponse.builder()
                                .status(HttpStatus.BAD_REQUEST.value())
                                .error("MALFORMED_JSON_REQUEST")
                                .message("Malformed JSON request")
                                .path(request.getRequestURI())
                                .build();

                return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }

        @ExceptionHandler(MethodArgumentNotValidException.class)
        public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex,
                        HttpServletRequest request) {
                String message = ex.getBindingResult().getFieldErrors()
                                .stream()
                                .map(err -> err.getField() + ": " + err.getDefaultMessage())
                                .collect(Collectors.joining(", "));

                ErrorResponse errorResponse = ErrorResponse.builder()
                                .status(HttpStatus.BAD_REQUEST.value())
                                .error("VALIDATION_FAILED")
                                .message(message)
                                .path(request.getRequestURI())
                                .build();

                return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }

        @ExceptionHandler(NotFoundException.class)
        public ResponseEntity<ErrorResponse> handleNotFound(NotFoundException ex, HttpServletRequest request) {
                ErrorResponse errorResponse = ErrorResponse.builder()
                                .status(HttpStatus.NOT_FOUND.value())
                                .error("RESOURCE_NOT_FOUND")
                                .message(ex.getMessage())
                                .path(request.getRequestURI())
                                .build();

                return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
        }

        @ExceptionHandler(UnauthorizedException.class)
        public ResponseEntity<ErrorResponse> handleUnauthorized(UnauthorizedException ex, HttpServletRequest request) {
                ErrorResponse errorResponse = ErrorResponse.builder()
                                .status(HttpStatus.UNAUTHORIZED.value())
                                .error("UNAUTHORIZED")
                                .message(ex.getMessage())
                                .path(request.getRequestURI())
                                .build();

                return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
        }

        @ExceptionHandler(EmailAlreadyExistsException.class)
        public ResponseEntity<ErrorResponse> handleEmailExists(EmailAlreadyExistsException ex,
                        HttpServletRequest request) {
                ErrorResponse errorResponse = ErrorResponse.builder()
                                .status(HttpStatus.CONFLICT.value())
                                .error("EMAIL_ALREADY_EXISTS")
                                .message(ex.getMessage())
                                .path(request.getRequestURI())
                                .build();

                return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
        }

        @ExceptionHandler(RoleNotFoundException.class)
        public ResponseEntity<ErrorResponse> handleRoleNotFound(RoleNotFoundException ex, HttpServletRequest request) {
                ErrorResponse errorResponse = ErrorResponse.builder()
                                .status(HttpStatus.NOT_FOUND.value())
                                .error("ROLE_NOT_FOUND")
                                .message(ex.getMessage())
                                .path(request.getRequestURI())
                                .build();

                return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
        }

        @ExceptionHandler(OtpSendingException.class)
        public ResponseEntity<ErrorResponse> handleOtpSendingException(OtpSendingException ex,
                        HttpServletRequest request) {
                ErrorResponse errorResponse = ErrorResponse.builder()
                                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                                .error("OTP_SENDING_FAILED")
                                .message(ex.getMessage())
                                .path(request.getRequestURI())
                                .build();

                return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }

        @ExceptionHandler(BadRequestException.class)
        public ResponseEntity<ErrorResponse> handleBadRequest(BadRequestException ex,
                        HttpServletRequest request) {
                ErrorResponse errorResponse = ErrorResponse.builder()
                                .status(HttpStatus.BAD_REQUEST.value())
                                .error("BAD_REQUEST")
                                .message(ex.getMessage())
                                .path(request.getRequestURI())
                                .build();

                return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }

        // Exception of booking
        @ExceptionHandler({
                        SeatAlreadyBookedException.class,
                        InvalidPromotionException.class,
                        InsufficientPointsException.class,
                        BookingValidationException.class,
                        PendingBookingExistsException.class
        })
        public ResponseEntity<ApiResponse<Object>> handleBookingExceptions(RuntimeException ex) {
                HttpStatus status = HttpStatus.BAD_REQUEST; // 400
                Object resultData = null;
                if (ex instanceof SeatAlreadyBookedException || ex instanceof PendingBookingExistsException) {
                        status = HttpStatus.CONFLICT; // 409
                        if (ex instanceof PendingBookingExistsException) {
                                // Returns the ID of the pending booking
                                resultData = Map.of("existingBookingId",
                                                ((PendingBookingExistsException) ex).getExistingBookingId());
                        }
                }
                ApiResponse<Object> apiResponse = ApiResponse.builder()
                                .status(status.value())
                                .message(ex.getMessage())
                                .result(resultData)
                                .build();
                return new ResponseEntity<>(apiResponse, status);
        }

        // Handle general Runtime errors (placed at the end)

        @ExceptionHandler(value = {
                        RuntimeException.class,
                        IllegalArgumentException.class,
        })
        public ResponseEntity<ApiResponse<Object>> handleApiResponseException(Exception ex) {
                ApiResponse<Object> apiResponse = ApiResponse.builder()
                                .status(HttpStatus.BAD_REQUEST.value())
                                .message(ex.getMessage())
                                .build();
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(apiResponse);
        }

        @ExceptionHandler(value = {
                        AppException.class,
        })
        public ResponseEntity<ErrorResponse> handleResponseException(Exception ex, HttpServletRequest request) {
                ErrorResponse errorResponse = ErrorResponse.builder()
                                .status(HttpStatus.BAD_REQUEST.value())
                                .error("An error occurred in the system!")
                                .message(ex.getMessage())
                                .path(request.getRequestURI())
                                .build();
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }


    @ExceptionHandler(DuplicateNameException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateName(DuplicateNameException ex, HttpServletRequest request) {
        ErrorResponse errorResponse = ErrorResponse.builder()
                .status(HttpStatus.CONFLICT.value())
                .error("Tên bị trùng")
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(FileStorageException.class)
    public ResponseEntity<ErrorResponse> handleFileStorageException(FileStorageException ex, HttpServletRequest request) {
        ErrorResponse errorResponse = ErrorResponse.builder()
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .error("File Storage Error")
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(SeatConversionException.class)
    public ResponseEntity<ErrorResponse> handleSeatConversion(SeatConversionException ex, HttpServletRequest request) {
        ErrorResponse errorResponse = ErrorResponse.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Lỗi chuyển đổi ghế")
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .build();
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(RoomNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleRoomNotFound(RoomNotFoundException ex, HttpServletRequest request) {
        ErrorResponse errorResponse = ErrorResponse.builder()
                .status(HttpStatus.NOT_FOUND.value())
                .error("Không tìm thấy phòng chiếu")
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .build();
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

}
