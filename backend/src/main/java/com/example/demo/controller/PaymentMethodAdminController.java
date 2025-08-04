package com.example.demo.controller;

import com.example.demo.model.PaymentMethod;
import com.example.demo.service.PaymentMethodService;

import lombok.AllArgsConstructor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

import java.util.List;

/**
 * REST controller for managing payment methods in the admin panel.
 * Allows CRUD operations and status toggling for payment methods.
 */
@CrossOrigin(origins = "http://localhost:3000")
@RestController
@AllArgsConstructor
@RequestMapping("/api/admin/payment-methods")
public class PaymentMethodAdminController {

    @Autowired
    private PaymentMethodService paymentMethodService;

    /**
     * Retrieves all payment methods, including both active and inactive ones.
     *
     * @return a list of all payment methods
     */
    @GetMapping
    public ResponseEntity<List<PaymentMethod>> getAll() {
        return ResponseEntity.ok(paymentMethodService.findAll());
    }

    /**
     * Creates a new payment method.
     *
     * @param method the payment method to be created
     * @return the created payment method with status 201 (CREATED)
     */
    @PostMapping("/create")
    public ResponseEntity<PaymentMethod> create(@RequestBody PaymentMethod method) {
        return new ResponseEntity<>(paymentMethodService.save(method), HttpStatus.CREATED);
    }

    /**
     * Updates the name and description of an existing payment method.
     *
     * @param id            the ID of the payment method to update
     * @param paymentMethod the updated payment method data
     * @return the updated payment method
     */
    @PutMapping("/{id}")
    public PaymentMethod update(@PathVariable Long id, @RequestBody PaymentMethod paymentMethod) {
        return paymentMethodService.update(id, paymentMethod);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PaymentMethod> getById(@PathVariable Long id) {
        PaymentMethod method = paymentMethodService.findById(id);
        if (method != null) {
            return ResponseEntity.ok(method);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Toggles the active status (enable/disable) of a payment method.
     *
     * @param id the ID of the payment method to toggle
     * @return the updated payment method with new active status
     */
    @PutMapping("/{id}/toggle")
    public PaymentMethod toggle(@PathVariable Long id) {
        return paymentMethodService.toggleActive(id);
    }

    /**
     * Deletes a payment method by its ID.
     *
     * @param id the ID of the payment method to delete
     */
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        paymentMethodService.delete(id);
    }
}
