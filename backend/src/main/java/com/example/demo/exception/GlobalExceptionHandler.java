package com.example.demo.exception;

import com.example.demo.DTO.response.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.example.demo.DTO.response.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import com.example.demo.exception.booking.*;

import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(NotFoundException ex, HttpServletRequest request) {
        ErrorResponse errorResponse = ErrorResponse.builder()
                .status(HttpStatus.NOT_FOUND.value())
                .error("Not Found")
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ErrorResponse> handleUnauthorized(UnauthorizedException ex, HttpServletRequest request) {
        ErrorResponse errorResponse = ErrorResponse.builder()
                .status(HttpStatus.UNAUTHORIZED.value())
                .error("Unauthorized")
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleEmailExists(EmailAlreadyExistsException ex, HttpServletRequest request) {
        ErrorResponse errorResponse = ErrorResponse.builder()
                .status(HttpStatus.CONFLICT.value())
                .error("Email already exists")
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
    }

    //Exception of booking
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
                resultData = Map.of("existingBookingId", ((PendingBookingExistsException) ex).getExistingBookingId());
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
}
