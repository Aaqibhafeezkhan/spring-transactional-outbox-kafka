package com.kholodilin.outbox.persistence;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;

/**
 * JDBC writes for {@code orders} and {@code order_items}.
 */
@Repository
@RequiredArgsConstructor
public class OrderJdbcRepository {

    private final JdbcTemplate jdbcTemplate;

    /**
     * Inserts an order header and returns the generated id.
     *
     * @param customerId  hash partition key
     * @param totalAmount sum of line items
     * @param now         created_at / updated_at
     * @return generated {@code orders.id}
     */
    public long insertOrder(Long customerId, BigDecimal totalAmount, Instant now) {
        Long id = jdbcTemplate.queryForObject(
                """
                        INSERT INTO orders (customer_id, status, total_amount, created_at, updated_at)
                        VALUES (?, ?, ?, ?, ?)
                        RETURNING id
                        """,
                Long.class,
                customerId,
                "CREATED",
                totalAmount,
                Timestamp.from(now),
                Timestamp.from(now)
        );
        return id;
    }

    /**
     * Inserts a single order line item.
     *
     * @param orderId    parent order id
     * @param customerId same customer as the order
     * @param productId  product id
     * @param quantity   units ordered
     * @param price      unit price
     * @param now        created_at
     */
    public void insertOrderItem(Long orderId, Long customerId, String productId, int quantity, BigDecimal price, Instant now) {
        jdbcTemplate.update(
                """
                        INSERT INTO order_items (order_id, customer_id, product_id, quantity, price, created_at)
                        VALUES (?, ?, ?, ?, ?, ?)
                        """,
                orderId,
                customerId,
                productId,
                quantity,
                price,
                Timestamp.from(now)
        );
    }
}
