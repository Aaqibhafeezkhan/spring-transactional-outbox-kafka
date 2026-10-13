package com.kholodilin.outbox.persistence;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.Instant;

/**
 * Insert-only JDBC writer for the CDC outbox. Rows are never updated after commit.
 */
@Repository
@RequiredArgsConstructor
public class OutboxJdbcRepository {

    private final JdbcTemplate jdbcTemplate;

    /**
     * Inserts one outbox row and returns its id.
     *
     * @param createdAt    partition key and retention timestamp
     * @param aggregateId  order id
     * @param partitionKey customer id, later used as the Kafka key
     * @param eventType    domain event name
     * @param payload      domain JSON
     * @param traceParent  W3C traceparent, or {@code null}
     * @return generated event id
     */
    public long insert(
            Instant createdAt,
            String aggregateId,
            String partitionKey,
            String eventType,
            String payload,
            String traceParent
    ) {
        Long id = jdbcTemplate.queryForObject(
                """
                        INSERT INTO outbox_events (
                            created_at, aggregate_id, partition_key, event_type, payload, trace_parent
                        )
                        VALUES (?, ?, ?, ?, CAST(? AS jsonb), ?)
                        RETURNING id
                        """,
                Long.class,
                Timestamp.from(createdAt),
                aggregateId,
                partitionKey,
                eventType,
                payload,
                traceParent
        );
        return id;
    }
}
