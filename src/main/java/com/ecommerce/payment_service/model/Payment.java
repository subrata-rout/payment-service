package com.ecommerce.payment_service.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Payment — JPA Entity representing a payment attempt.
 *
 * WHY separate from Order:
 *   One order can have MULTIPLE payment attempts.
 *   Example: user tries card → fails → retries → succeeds
 *   Both attempts are separate Payment records for the same Order.
 *   This gives us full audit trail — critical for financial systems.
 *
 * DB Table: payments
 *
 * Relationships:
 *   Many payments → One order (@ManyToOne → order_id FK)
 *
 * Real flow:
 *   1. Order created (status=PENDING)
 *   2. Payment created (status=INITIATED)
 *   3. Gateway called → returns transactionId
 *   4. Payment updated (status=SUCCESS or FAILED)
 *   5. Order updated (status=CONFIRMED or CANCELLED)
 */

@Entity
@Table(name="payments")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * WHY @ManyToOne to Order (not User directly):
     *   Payment is always for a specific order, not a user.
     *   User is derived: payment.getOrder().getUser()
     *   This keeps relationships clean and normalized (3NF)
     */

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    /**
     * Which payment gateway processed this payment.
     * WHY store this: if Stripe has an outage, we know which payments
     * to retry. Also needed for refunds — must use same gateway.
     */

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentGateway gateway;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status;



    /**
     * transactionId — ID returned by the payment gateway after processing.
     * WHY unique = true:
     *   Each gateway transaction is globally unique.
     *   Prevents double-processing the same transaction (idempotency).
     *   This is critical in payment systems — charging twice = serious bug.
     */
    @Column(name = "transaction_id", unique = true)
    private String transactionId;

    @Column(precision = 10, scale=2)
    private BigDecimal amount;

    /**
     * Supported payment gateways.
     * WHY enum not String:
     *   Prevents typos like "strpe" or "STRIPE " (trailing space)
     *   Compile-time safety — invalid gateway = compile error not runtime bug
     */

    /**
     * WHY store currency:
     *   We support INR, USD, EUR etc.
     *   Amount 100 means nothing without knowing if it's ₹100 or $100
     */
    @Column(length = 3)
    // length=3: ISO 4217 currency codes are always 3 chars (INR, USD, EUR)
    private String currency;

    @Column(name = "failure_reason")
    // WHY store failure reason: helps debug and show user why payment failed
    private String failureReason;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null) status = PaymentStatus.INITIATED;
        if (currency == null) currency = "INR";
    }

    @PreUpdate
    // WHY @PreUpdate: runs before every UPDATE — keeps updatedAt accurate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * Supported payment gateways.
     * WHY enum not String:
     *   Prevents typos like "strpe" or "STRIPE " (trailing space)
     *   Compile-time safety — invalid gateway = compile error not runtime bug
     */
    public enum PaymentGateway {
        STRIPE,
        RAZORPAY,
        PAYPAL
    }

    /**
     * Payment lifecycle states.
     *   INITIATED → payment record created in our DB
     *   PENDING   → request sent to gateway, waiting for response
     *   SUCCESS   → gateway confirmed payment received
     *   FAILED    → gateway rejected or timeout
     *   REFUNDED  → money returned to customer
     */
    public enum PaymentStatus {
        INITIATED,
        PENDING,
        SUCCESS,
        FAILED,
        REFUNDED
    }

}

