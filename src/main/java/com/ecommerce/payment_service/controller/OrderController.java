package com.ecommerce.payment_service.controller;

/**
 * OrderController — HTTP layer for Order operations.
 *
 * WHY versioned URL (/api/v1/orders):
 *   When we release breaking changes, we create /api/v2/orders.
 *   Existing clients using /v1/ keep working — no forced migration.
 *   Industry standard for production APIs.
 *
 * Layer: (we are here) → OrderService → OrderRepository → DB
 */

import com.ecommerce.payment_service.dto.OrderDTO;
import com.ecommerce.payment_service.service.OrderService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    // POST /api/v1/orders — create a new order
    @PostMapping
    public ResponseEntity<OrderDTO> createOrder(@Valid @RequestBody OrderDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(orderService.createOrder(dto));
    }

    // GET /api/v1/orders/{id} — get single order
    @GetMapping("/{id}")
    public ResponseEntity<OrderDTO> getOrder(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.getOrderById(id));
    }

    // GET /api/v1/orders — get all orders
    @GetMapping
    public ResponseEntity<List<OrderDTO>> getAllOrders() {
        return ResponseEntity.ok(orderService.getAllOrders());
    }

    // GET /api/v1/orders/user/{userId} — get all orders for a user
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<OrderDTO>> getOrdersByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(orderService.getOrdersByUser(userId));
    }
}