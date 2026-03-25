package com.ecommerce.payment_service.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentDTO {

/**
 * PaymentDTO — Data Transfer Object for Payment API.
 *
 * Used for:
 *   REQUEST  → client sends orderId, gateway, amount, currency
 *   RESPONSE → server returns id, status, transactionId, timestamps
 *
 * WHY gateway is String in DTO but Enum in Entity:
 *   API receives "STRIPE" as a string from JSON.
 *   Service converts it to PaymentGateway.STRIPE enum before saving.
 *   This gives better error messages: "Invalid gateway: STRIPEE"
 *   instead of a cryptic JSON parse error.
 */

        private Long id;            // null on request, set by server on response

        @NotNull(message = "Order ID is required")
        private Long orderId;

        @NotNull(message = "Gateway is required — STRIPE, RAZORPAY or PAYPAL")
        private String gateway;

        @NotNull(message = "Amount is required")
        @Positive(message = "Amount must be greater than zero")
        private BigDecimal amount;

        private String currency;        // defaults to INR if not provided
        private String status;          // set by server
        private String transactionId;   // returned by gateway, set by server
        private String failureReason;   // set by server if payment fails
        private String createdAt;

}
