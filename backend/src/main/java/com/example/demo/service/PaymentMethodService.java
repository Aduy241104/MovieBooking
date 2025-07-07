package com.example.demo.service;

import com.example.demo.model.PaymentMethod;
import com.example.demo.repository.PaymentMethodRepository;

import org.springframework.data.domain.Sort;
import lombok.AllArgsConstructor;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class PaymentMethodService {

    @Autowired
    private PaymentMethodRepository paymentMethodRepository;

    // Lấy tất cả phương thức thanh toán
    public List<PaymentMethod> findAll() {
        return paymentMethodRepository.findAll(Sort.by(Sort.Direction.ASC, "id"));
    }

    // Tạo mới phương thức thanh toán
    public PaymentMethod save(PaymentMethod paymentMethod) {
        // Nếu client không truyền active, thì mặc định là true
        if (paymentMethod.getActive() == null) {
            paymentMethod.setActive(true); // Chỉ set mặc định nếu không được gửi từ client
        } 

        if (paymentMethodRepository.existsByNameIgnoreCase(paymentMethod.getName())) {
        throw new RuntimeException("Tên phương thức thanh toán đã tồn tại.");
    }
        
        return paymentMethodRepository.save(paymentMethod);
    }

    // Cập nhật tên và mô tả
    public PaymentMethod update(Long id, PaymentMethod updated) {
        PaymentMethod existing = paymentMethodRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phương thức thanh toán"));
        existing.setName(updated.getName());
        existing.setDescription(updated.getDescription());
        return paymentMethodRepository.save(existing);
    }

    // Bật/tắt trạng thái hoạt động
    public PaymentMethod toggleActive(Long id) {
        PaymentMethod method = paymentMethodRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phương thức thanh toán"));
        method.setActive(!method.getActive());
        return paymentMethodRepository.save(method);
    }

    // Xóa phương thức thanh toán
    public void delete(Long id) {
        paymentMethodRepository.deleteById(id);
    }

    public List<PaymentMethod> getActivePaymentMethods() {
        return paymentMethodRepository.findByActiveTrue();
    }
}


