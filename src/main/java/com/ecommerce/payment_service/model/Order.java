package com.ecommerce.payment_service.model;
/*Order — JPA Entity representing a customer order.
        *
        * WHY this entity exists:
        *   An order is the TRIGGER for a payment. Before any payment happens,
        *   an order must exist. This is the real-world flow:
        *   User places Order → Order triggers Payment → Payment contacts Gateway
        *
        * DB Table: orders
        *
        * Relationships:
        *   Many orders belong to ONE user (@ManyToOne → user_id FK in orders table)
        *   One order can have MANY payments (retry scenarios, partial payments)
        *
        * SOLID applied:
        *   Single Responsibility — this class ONLY represents order data.
        *   No business logic here. Logic lives in OrderService.
*/

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name="orders")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    // IDENTITY = auto-increment in MySQL (1, 2, 3...)
    // WHY not UUID here: numeric IDs are faster for joins and indexing

    private Long id;
    /**
     * WHY @ManyToOne with FetchType.LAZY:
     *   Many orders belong to one user.
     *   LAZY means: don't load User data from DB unless we explicitly call getUser()
     *   EAGER (the default) would load User on every Order query — wasteful
     *   @ JoinColumn creates the foreign key column user_id in orders table
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /*
     * WHY BigDecimal for money — NEVER use double or float:
     *   double: 0.1 + 0.2 = 0.30000000000000004 (floating point error)
     *   BigDecimal: 0.1 + 0.2 = 0.3 (exact)
     *   In payments, even 1 paisa error = serious financial bug
     *   precision=10 means 10 total digits, scale=2 means 2 decimal places
     *   Max value: 99999999.99
     */

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    /**
     * WHY @Enumerated(EnumType.STRING):
     *   EnumType.ORDINAL stores 0,1,2 — if you reorder the enum, data corrupts
     *   EnumType.STRING stores "PENDING","CONFIRMED" — safe, readable in DB
     */

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;

    private String description;

    @Column(name="created_at", updatable = false)
    //updatable=false: once set, this column never changes on UPDATE
    private LocalDateTime createdAt;

    /**
     * @ PrePersist runs automatically BEFORE every INSERT.
     * WHY here and not in service:
     *   This is entity lifecycle logic — it belongs with the entity.
     *   The service shouldn't need to remember to set these fields.
     */
    @PrePersist
    protected void onCreate(){
        createdAt=LocalDateTime.now();
        if(status== null){
            status=OrderStatus.PENDING; // every new order starts as pending
        }
    }

    /**
     * Order lifecycle:
     *   PENDING → order created, awaiting payment
     *   CONFIRMED → payment successful
     *   CANCELLED → user cancelled or payment failed
     *   COMPLETED → order delivered/fulfilled
     * WHY inner enum: tightly coupled to Order, no reason to live separately
     */
    public enum OrderStatus {
        PENDING,
        CONFIRMED,
        CANCELLED,
        COMPLETED
    }
}
