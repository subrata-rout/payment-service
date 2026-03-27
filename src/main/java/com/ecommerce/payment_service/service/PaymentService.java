package com.ecommerce.payment_service.service;

/**
 * PaymentService — Business logic for Payment operations.
 *
 * WHY this is the most important service in the project:
 *   This is the core of the entire application.
 *   Every other service exists to support this one.
 *
 * Responsibilities:
 *   - Initiate payment (create Payment record, call gateway — Day 3)
 *   - Update payment status when gateway responds (webhook)
 *   - Update the linked Order status after payment result
 *   - Handle refunds
 *
 * For now (Day 2): we simulate gateway calls.
 * Day 3: we plug in real Stripe/Razorpay/PayPal SDKs.
 *
 * Layer: Controller → PaymentService → PaymentRepository
 *                                    → OrderRepository (update order status)
 */

import com.ecommerce.payment_service.dto.PaymentDTO;
import com.ecommerce.payment_service.model.Order;
import com.ecommerce.payment_service.model.Payment;
import com.ecommerce.payment_service.repository.OrderRepository;
import com.ecommerce.payment_service.repository.PaymentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    // WHY OrderRepository: after payment, we update the order status
    // Payment and Order are tightly linked — payment result drives order state

    public PaymentService(PaymentRepository paymentRepository,
                          OrderRepository orderRepository) {
        this.paymentRepository = paymentRepository;
        this.orderRepository = orderRepository;
    }

    /**
     * Initiates a payment for an existing order.
     *
     * Current implementation: simulates gateway (returns fake transaction ID)
     * Day 3 implementation: calls real Stripe/Razorpay/PayPal API
     *
     * WHY @Transactional here:
     *   We update TWO tables — payments and orders.
     *   If payment saves but order update fails → data inconsistency.
     *   @ Transactional ensures both succeed or both roll back.
     *   This is the foundation of the SAGA pattern we implement on Day 4.
     */
    @Transactional
    public PaymentDTO initiatePayment(PaymentDTO dto) {
        // Step 1: Validate order exists
        Order order = orderRepository.findById(dto.getOrderId())
                .orElseThrow(() -> new RuntimeException(
                        "Order not found: " + dto.getOrderId()));

        // Step 2: Validate order is in a payable state
        // WHY this check: can't pay for a CANCELLED or already CONFIRMED order
        if (order.getStatus() == Order.OrderStatus.CONFIRMED) {
            throw new RuntimeException("Order already paid: " + dto.getOrderId());
        }
        if (order.getStatus() == Order.OrderStatus.CANCELLED) {
            throw new RuntimeException("Cannot pay for cancelled order: " + dto.getOrderId());
        }

        // Step 3: Parse gateway from string → enum (with helpful error message)
        Payment.PaymentGateway gateway;
        try {
            gateway = Payment.PaymentGateway.valueOf(dto.getGateway().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException(
                    "Invalid gateway: " + dto.getGateway() +
                            ". Valid values: STRIPE, RAZORPAY, PAYPAL");
        }

        // Step 4: Create Payment record in DB (status = INITIATED)

        Payment payment = new Payment();
        payment.setOrder(order);
        payment.setGateway(gateway);
        payment.setAmount(dto.getAmount() != null ? dto.getAmount() : order.getAmount());
        payment.setCurrency(dto.getCurrency() != null ? dto.getCurrency() : "INR");

        // Step 5: Simulate gateway call
        // WHY UUID: globally unique, same format real gateways use
        // replace this with actual Stripe/Razorpay API call
        String simulatedTransactionId = gateway.name() + "_" +
                UUID.randomUUID().toString().replace("-", "").substring(0, 16).toUpperCase();

        payment.setTransactionId(simulatedTransactionId);
        payment.setStatus(Payment.PaymentStatus.SUCCESS); // simulated success

        Payment saved = paymentRepository.save(payment);

        // Step 6: Update order status based on payment result
        // WHY here and not in OrderService:
        //   PaymentService owns the payment result → it drives the order state change
        //   This is the SAGA orchestration pattern preview
        order.setStatus(Order.OrderStatus.CONFIRMED);
        orderRepository.save(order);

        return toDTO(saved);
    }

    // Get all payments for a specific order (shows retry history)
    public List<PaymentDTO> getPaymentsByOrder(Long orderId) {
        return paymentRepository.findByOrderId(orderId)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    // Get single payment by ID
    public PaymentDTO getPaymentById(Long id) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Payment not found: " + id));
        return toDTO(payment);
    }

    /**
     * Converts Payment entity → PaymentDTO.
     * WHY we return orderId not the full Order:
     *   Client only needs the reference, not the entire order object.
     *   Keeps the response clean and avoids over-fetching.
     */
    private PaymentDTO toDTO(Payment payment) {
        return new PaymentDTO(
                payment.getId(),
                payment.getOrder().getId(),
                payment.getGateway().name(),
                payment.getAmount(),
                payment.getCurrency(),
                payment.getStatus().name(),
                payment.getTransactionId(),
                payment.getFailureReason(),
                payment.getCreatedAt() != null ? payment.getCreatedAt().toString() : null
        );
    }
}