package com.ecommerce.payment_service.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;


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

        public PaymentDTO() {
        }

        public PaymentDTO(Long id, Long orderId, String gateway, BigDecimal amount, String currency, String status, String transactionId, String failureReason, String createdAt) {
                this.id = id;
                this.orderId = orderId;
                this.gateway = gateway;
                this.amount = amount;
                this.currency = currency;
                this.status = status;
                this.transactionId = transactionId;
                this.failureReason = failureReason;
                this.createdAt = createdAt;
        }

        public Long getId() {
                return id;
        }

        public Long getOrderId() {
                return orderId;
        }

        public String getGateway() {
                return gateway;
        }

        public BigDecimal getAmount() {
                return amount;
        }

        public String getCurrency() {
                return currency;
        }

        public String getStatus() {
                return status;
        }

        public String getTransactionId() {
                return transactionId;
        }

        public String getFailureReason() {
                return failureReason;
        }

        public String getCreatedAt() {
                return createdAt;
        }


        public void setId(Long id) {
                this.id = id;
        }

        public void setOrderId(Long orderId) {
                this.orderId = orderId;
        }

        public void setGateway(String gateway) {
                this.gateway = gateway;
        }

        public void setAmount(BigDecimal amount) {
                this.amount = amount;
        }

        public void setCurrency(String currency) {
                this.currency = currency;
        }

        public void setStatus(String status) {
                this.status = status;
        }

        public void setTransactionId(String transactionId) {
                this.transactionId = transactionId;
        }

        public void setFailureReason(String failureReason) {
                this.failureReason = failureReason;
        }

        public void setCreatedAt(String createdAt) {
                this.createdAt = createdAt;
        }
}
