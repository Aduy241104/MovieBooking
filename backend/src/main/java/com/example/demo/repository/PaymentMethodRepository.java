package com.example.demo.repository;


import com.example.demo.model.PaymentMethod;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

import java.util.Optional;

@Repository
public interface PaymentMethodRepository extends JpaRepository<PaymentMethod, Long> {

    // Find all active payment methods
    List<PaymentMethod> findByActiveTrue();

    // Find a payment method by name (case-insensitive)
    PaymentMethod findByNameIgnoreCase(String name);

    // Find active payment methods by type (e.g., CARD, EWALLET, CASH)
    List<PaymentMethod> findByNameIgnoreCaseAndActiveTrue(String type);

    // Check if a payment method with the given name exists (case-insensitive)
    boolean existsByNameIgnoreCase(String name);

    // Find a payment method by ID only if it is active
    Optional<PaymentMethod> findByIdAndActiveTrue(Long id);
}

