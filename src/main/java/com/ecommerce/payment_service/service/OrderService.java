package com.ecommerce.payment_service.service;

/**
 * OrderService — Business logic for Order operations.
 *
 * Responsibilities:
 *   - Create orders linked to existing users
 *   - Retrieve orders (by id, by user, all)
 *   - Update order status (called by PaymentService after payment result)
 *   - Cancel orders
 *
 * WHY OrderService does NOT process payments:
 *   Single Responsibility Principle — one class, one job.
 *   Payment logic lives in PaymentService.
 *   OrderService only manages order lifecycle.
 *
 * Layer: Controller → OrderService → OrderRepository → orders table
 */

import com.ecommerce.payment_service.dto.OrderDTO;
import com.ecommerce.payment_service.model.Order;
import com.ecommerce.payment_service.model.User;
import com.ecommerce.payment_service.repository.OrderRepository;
import com.ecommerce.payment_service.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    // WHY UserRepository here: we need to validate user exists before creating order
    // We don't inject UserService — services should not call other services directly
    // (causes circular dependencies). Repositories are safe to share.

    public OrderService(OrderRepository orderRepository,
                        UserRepository userRepository) {
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
    }

    /**
     * Creates a new order for an existing user.
     *
     * WHY @Transactional:
     *   If anything fails mid-method, ALL DB changes are rolled back.
     *   Without it: user lookup succeeds, order save fails →
     *   we have a broken half-state in the DB.
     *   With it: either everything succeeds or nothing changes.
     */
    @Transactional
    public OrderDTO createOrder(OrderDTO dto) {
        // Validate user exists before creating order
        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new RuntimeException(
                        "User not found with id: " + dto.getUserId()));

        // Build the Order entity from DTO
        Order order = Order.builder()
                .user(user)             // full User object, not just the ID
                .amount(dto.getAmount())
                .description(dto.getDescription())
                // status and createdAt set automatically by @PrePersist
                .build();

        Order saved = orderRepository.save(order);
        return toDTO(saved);
    }

    /**
     * Get single order by ID.
     * WHY orElseThrow: Optional forces explicit "not found" handling.
     */
    public OrderDTO getOrderById(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found: " + id));
        return toDTO(order);
    }

    // Get all orders for a specific user
    public List<OrderDTO> getOrdersByUser(Long userId) {
        return orderRepository.findByUserId(userId)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    // Get all orders in the system
    public List<OrderDTO> getAllOrders() {
        return orderRepository.findAll()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Updates order status.
     * WHY called by PaymentService after payment result:
     *   When payment succeeds → order becomes CONFIRMED
     *   When payment fails → order stays PENDING (can retry)
     *   When user cancels → order becomes CANCELLED
     */
    @Transactional
    public OrderDTO updateOrderStatus(Long id, Order.OrderStatus newStatus) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found: " + id));
        order.setStatus(newStatus);
        return toDTO(orderRepository.save(order));
    }

    /**
     * Converts Order entity → OrderDTO.
     *
     * WHY we extract user.getId() not the whole user:
     *   DTO only needs userId for the response.
     *   Returning the full User object would expose all user data
     *   in every order response — unnecessary and potentially unsafe.
     */
    private OrderDTO toDTO(Order order) {
        return OrderDTO.builder()
                .id(order.getId())
                .userId(order.getUser().getId())
                .amount(order.getAmount())
                .status(order.getStatus().name())
                .description(order.getDescription())
                .createdAt(order.getCreatedAt() != null ?
                        order.getCreatedAt().toString() : null)
                .build();
    }
}