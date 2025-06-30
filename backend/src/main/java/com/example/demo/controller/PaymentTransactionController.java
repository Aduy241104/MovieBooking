package com.example.demo.controller;

import com.example.demo.DTO.response.PaymentTransactionResponseDTO;
import com.example.demo.service.PaymentTransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/admin/payment-transactions")
@CrossOrigin("*") // Nếu cần cho FE gọi từ khác domain
public class PaymentTransactionController {

    @Autowired
    private PaymentTransactionService paymentTransactionService;

    @GetMapping
    public Page<PaymentTransactionResponseDTO> getPagedTransactions(
            @RequestParam(defaultValue = "0") int page, 
            @RequestParam(defaultValue = "10") int size) {
        return paymentTransactionService.getAllTransactions(page, size);
    }

}
