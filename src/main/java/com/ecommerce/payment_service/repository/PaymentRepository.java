package com.ecommerce.payment_service.repository;

/**
 * PaymentRepository — Data access layer for Payment entity.
 *
 * Key custom queries:
 *   findByOrderId     — all payment attempts for one order (retry history)
 *   findByTransactionId — lookup by gateway's transaction ID (for webhooks)
 *   findByStatus      — all failed payments (for retry jobs)
 */

import com.ecommerce.payment_service.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    // All payment attempts for a given order (could be multiple on retry)
    List<Payment> findByOrderId(Long orderId);

    // WHY Optional: transaction ID might not match anything — handle gracefully
    // Used when Stripe/Razorpay webhook arrives with a transaction ID
    Optional<Payment> findByTransactionId(String transactionId);

    // Find all payments by status — useful for retry jobs and reporting
    List<Payment> findByStatus(Payment.PaymentStatus status);
}