package com.example.demo.service;

import com.example.demo.model.PaymentMethod;
import com.example.demo.repository.PaymentMethodRepository;

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
        return paymentMethodRepository.findAll();
    }

    // Tạo mới phương thức thanh toán
    public PaymentMethod save(PaymentMethod paymentMethod) {
        paymentMethod.setActive(true); // Mặc định là active
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
}
