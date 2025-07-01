package com.example.demo.repository;

import com.example.demo.model.PaymentMethod;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PaymentMethodRepository extends JpaRepository<PaymentMethod, Long> {

    // Tìm các phương thức thanh toán đang hoạt động
    List<PaymentMethod> findByActiveTrue();

    // Tìm phương thức thanh toán theo tên
    PaymentMethod findByNameIgnoreCase(String name);

    // Tìm phương thức thanh toán theo loại (CARD, EWALLET, CASH, etc.)
    List<PaymentMethod> findByNameIgnoreCaseAndActiveTrue(String type);

    // Kiểm tra tồn tại phương thức thanh toán
    boolean existsByNameIgnoreCase(String name);
}
