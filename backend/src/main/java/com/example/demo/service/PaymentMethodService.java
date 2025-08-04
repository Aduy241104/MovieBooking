package com.example.demo.service;

import com.example.demo.model.PaymentMethod;
import com.example.demo.repository.PaymentMethodRepository;
import com.example.demo.exception.BadRequestException;
import com.example.demo.exception.NotFoundException;

import org.springframework.data.domain.Sort;
import lombok.AllArgsConstructor;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service class that handles business logic for managing payment methods.
 */
@Service
@AllArgsConstructor
public class PaymentMethodService {

    @Autowired
    private PaymentMethodRepository paymentMethodRepository;

    /**
     * Retrieves all payment methods sorted by ID in ascending order.
     *
     * @return a list of all {@link PaymentMethod}
     */
    public List<PaymentMethod> findAll() {
        return paymentMethodRepository.findAll(Sort.by(Sort.Direction.ASC, "id"));
    }

    /**
     * Creates a new payment method.
     * If the `active` field is not provided, it defaults to `true`.
     *
     * @param paymentMethod the payment method to create
     * @return the saved {@link PaymentMethod}
     * @throws BadRequestException if the name already exists (case-insensitive)
     */
    public PaymentMethod save(PaymentMethod paymentMethod) {
        // Nếu client không truyền active, thì mặc định là true
        if (paymentMethod.getActive() == null) {
            paymentMethod.setActive(true); // Chỉ set mặc định nếu không được gửi từ client
        }

        if (paymentMethodRepository.existsByNameIgnoreCase(paymentMethod.getName())) {
            throw new BadRequestException("Tên phương thức thanh toán đã tồn tại.");
        }

        return paymentMethodRepository.save(paymentMethod);
    }

    /**
     * Updates the name and description of a payment method.
     *
     * @param id      the ID of the payment method to update
     * @param updated the new values to apply
     * @return the updated {@link PaymentMethod}
     * @throws NotFoundException    if the method is not found
     * @throws BadRequestException  if the new name is already used by another method
     */
    public PaymentMethod update(Long id, PaymentMethod updated) {
        PaymentMethod existing = paymentMethodRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy phương thức thanh toán"));

        if (!existing.getName().equalsIgnoreCase(updated.getName()) &&
                paymentMethodRepository.existsByNameIgnoreCase(updated.getName())) {
            throw new BadRequestException("Tên phương thức thanh toán đã tồn tại.");
        }
        existing.setName(updated.getName());
        existing.setDescription(updated.getDescription());
        return paymentMethodRepository.save(existing);
    }

    /**
     * Toggles the `active` status of a payment method.
     *
     * @param id the ID of the payment method
     * @return the updated {@link PaymentMethod}
     * @throws NotFoundException if the payment method is not found
     */
    public PaymentMethod toggleActive(Long id) {
        PaymentMethod method = paymentMethodRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy phương thức thanh toán"));
        method.setActive(!method.getActive());
        return paymentMethodRepository.save(method);
    }

    /**
     * Deletes a payment method by ID.
     *
     * @param id the ID of the payment method to delete
     */
    public void delete(Long id) {
        paymentMethodRepository.deleteById(id);
    }

    /**
     * Retrieves all active payment methods.
     *
     * @return a list of active {@link PaymentMethod}
     */
    public List<PaymentMethod> getActivePaymentMethods() {
        return paymentMethodRepository.findByActiveTrue();
    }

    public PaymentMethod findById(Long id) {
    return paymentMethodRepository.findById(id).orElse(null);
}
}
