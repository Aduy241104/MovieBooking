package com.example.demo.controller;

import com.example.demo.DTO.response.ApiResponse;
import com.example.demo.model.PaymentMethod;
import com.example.demo.service.PaymentMethodService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST controller for exposing active payment methods to the public.
 * Allows unauthenticated users to view available payment options.
 */
@RestController
@RequestMapping("/api/public/payment-methods") // Để public để ai cũng xem được các phương thức
@CrossOrigin
@RequiredArgsConstructor
public class PaymentMethodController {
    private final PaymentMethodService paymentMethodService;

    /**
     * Retrieves a list of currently active payment methods.
     *
     * @return an {@link ApiResponse} containing the list of active payment methods
     *         along with status and message
     */
    @GetMapping
    public ApiResponse<List<PaymentMethod>> getActivePaymentMethods() {
        return ApiResponse.<List<PaymentMethod>>builder()
                .status(HttpStatus.OK.value())
                .message("Successfully fetched active payment methods.")
                .result(paymentMethodService.getActivePaymentMethods())
                .build();
    }
}