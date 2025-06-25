package com.example.demo.controller;

import com.example.demo.model.PaymentMethod;
import com.example.demo.service.PaymentMethodService;

import lombok.AllArgsConstructor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

import java.util.List;

@CrossOrigin(origins = "http://localhost:3000")
@RestController@AllArgsConstructor
@RequestMapping("/api/admin/payment-methods")
public class PaymentMethodAdminController {

    @Autowired
    private PaymentMethodService paymentMethodService;

    // Lấy tất cả phương thức (cả active & inactive)
    @GetMapping
    public ResponseEntity<List<PaymentMethod>> getAll() {
        return ResponseEntity.ok(paymentMethodService.findAll());
    }

    // Thêm mới phương thức
    @PostMapping("/create")
    public ResponseEntity<PaymentMethod> create(@RequestBody PaymentMethod method) {
        return new ResponseEntity<>(paymentMethodService.save(method), HttpStatus.CREATED);
    }

    // Cập nhật tên và mô tả
    @PutMapping("/{id}")
    public PaymentMethod update(@PathVariable Long id, @RequestBody PaymentMethod paymentMethod) {
        return paymentMethodService.update(id, paymentMethod);
    }

    // Bật/tắt trạng thái hoạt động
    @PutMapping("/{id}/toggle")
    public PaymentMethod toggle(@PathVariable Long id) {
        return paymentMethodService.toggleActive(id);
    }

    // Xóa phương thức thanh toán
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        paymentMethodService.delete(id);
    }
}
