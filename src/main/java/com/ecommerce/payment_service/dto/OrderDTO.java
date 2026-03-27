package com.ecommerce.payment_service.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * OrderDTO — Data Transfer Object for Order API requests and responses.

 * WHY DTO instead of returning Order entity directly:
 *   1. Entity has @ManyToOne User — serializing it causes infinite recursion
 *      (User has orders, Order has User, User has orders... StackOverflow)
 *   2. Client doesn't need createdAt, internal status transitions etc.
 *   3. We control exactly what goes in and out of our API
 *   4. DB schema can change without breaking the API contract

 * This is the Interface Segregation Principle (I in SOLID):
 *   Clients only see what they need, nothing more.
 */



public class OrderDTO {
    // id is null on CREATE requests, populated in responses
    private Long id;

    @NotNull(message = "User ID is required")
    // WHY Long not int: IDs should handle large numbers (millions of users)
    private Long userId;

@NotNull(message = "Amount is required")
@Positive(message = "Amount must be greater than zero")
// @Positive: Spring validates BEFORE the request reaches the service
// Bad data never enters our business logic layer
private BigDecimal amount;

    private String status;      // read-only in responses, set by server
    private String description;
    private String createdAt;   // String for clean API response formatting

    public OrderDTO() {}

    public OrderDTO(Long id, Long userId, BigDecimal amount,
                    String status, String description, String createdAt) {
        this.id = id;
        this.userId = userId;
        this.amount = amount;
        this.status = status;
        this.description = description;
        this.createdAt = createdAt;
    }

    // Getters — Spring needs these to read field values
    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public BigDecimal getAmount() { return amount; }
    public String getStatus() { return status; }
    public String getDescription() { return description; }
    public String getCreatedAt() { return createdAt; }

    // Setters — Spring needs these to populate from JSON
    public void setId(Long id) { this.id = id; }
    public void setUserId(Long userId) { this.userId = userId; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public void setStatus(String status) { this.status = status; }
    public void setDescription(String description) { this.description = description; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}