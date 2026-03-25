package com.ecommerce.payment_service.controller;

/**
 * PaymentController — HTTP layer for Payment operations.
 *
 * Endpoints:
 *   POST /api/v1/payments         — initiate a payment
 *   GET  /api/v1/payments/{id}    — get payment by ID
 *   GET  /api/v1/payments/order/{orderId} — all payments for an order
 *
 * Layer: (we are here) → PaymentService → PaymentRepository → DB
 */

import com.ecommerce.payment_service.dto.PaymentDTO;
import com.ecommerce.payment_service.service.PaymentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/v1/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    // POST /api/v1/payments — initiate payment for an order
    @PostMapping
    public ResponseEntity<PaymentDTO> initiatePayment(@Valid @RequestBody PaymentDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(paymentService.initiatePayment(dto));
    }

    // GET /api/v1/payments/{id} — get payment details
    @GetMapping("/{id}")
    public ResponseEntity<PaymentDTO> getPayment(@PathVariable Long id) {
        return ResponseEntity.ok(paymentService.getPaymentById(id));
    }

    // GET /api/v1/payments/order/{orderId} — all payments for an order
    @GetMapping("/order/{orderId}")
    public ResponseEntity<List<PaymentDTO>> getPaymentsByOrder(@PathVariable Long orderId) {
        return ResponseEntity.ok(paymentService.getPaymentsByOrder(orderId));
    }
}