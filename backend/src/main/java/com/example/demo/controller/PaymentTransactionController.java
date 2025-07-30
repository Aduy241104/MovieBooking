package com.example.demo.controller;

import com.example.demo.DTO.response.PaymentTransactionResponseDTO;
import com.example.demo.service.PaymentTransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;


/**
 * REST controller for managing payment transactions in the admin panel.
 * Provides an endpoint to retrieve paginated lists of payment transactions.
 */
@RestController
@RequestMapping("/api/admin/payment-transactions")
@CrossOrigin("*") // Allow requests from any domain (for frontend integration)
public class PaymentTransactionController {

    @Autowired
    private PaymentTransactionService paymentTransactionService;

    /**
     * Retrieves a paginated list of payment transactions.
     *
     * @param page the page number to retrieve (default is 0)
     * @param size the number of items per page (default is 10)
     * @return a paginated list of payment transactions
     */
    @GetMapping
    public Page<PaymentTransactionResponseDTO> getPagedTransactions(
            @RequestParam(defaultValue = "0") int page, 
            @RequestParam(defaultValue = "10") int size) {
        return paymentTransactionService.getAllTransactions(page, size);
    }

}
