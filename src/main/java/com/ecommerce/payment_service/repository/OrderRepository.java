package com.ecommerce.payment_service.repository;

/**
 * OrderRepository — Data access layer for Order entity.
 *
 * WHY extend JpaRepository<Order, Long>:
 *   JpaRepository gives us for FREE — zero SQL needed:
 *   save(), findById(), findAll(), deleteById(), count(), existsById()
 *
 * WHY we add custom methods:
 *   findByUserId — get all orders for a specific user
 *   findByStatus — admin dashboard filtering by order status
 *
 * HOW Spring generates SQL from method names (Derived Queries):
 *   findByUserId(Long userId)
 *   → SELECT * FROM orders WHERE user_id = ?
 *
 *   findByStatus(OrderStatus status)
 *   → SELECT * FROM orders WHERE status = ?
 *
 *   No SQL written by us. Spring reads the method name and generates it.
 *   This is the Repository pattern — hides DB details from the service.
 */

import com.ecommerce.payment_service.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

    @Repository
    public interface OrderRepository extends JpaRepository<Order, Long> {

        // Derived query → SELECT * FROM orders WHERE user_id = ?
        List<Order> findByUserId(Long userId);

        // Derived query → SELECT * FROM orders WHERE status = ?
        List<Order> findByStatus(Order.OrderStatus status);

        // Derived query → SELECT * FROM orders WHERE user_id = ? AND status = ?
        List<Order> findByUserIdAndStatus(Long userId, Order.OrderStatus status);

}
