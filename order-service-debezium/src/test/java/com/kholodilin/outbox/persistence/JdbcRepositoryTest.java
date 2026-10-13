package com.kholodilin.outbox.persistence;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;

import java.math.BigDecimal;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JdbcRepositoryTest {

    @Mock
    private JdbcTemplate jdbcTemplate;

    @Test
    void insertOrderReturnsGeneratedIdAndWritesItem() {
        when(jdbcTemplate.queryForObject(anyString(), eq(Long.class), any(), any(), any(), any(), any()))
                .thenReturn(7L);
        OrderJdbcRepository repository = new OrderJdbcRepository(jdbcTemplate);
        Instant now = Instant.parse("2026-10-13T08:00:00Z");

        assertThat(repository.insertOrder(3L, BigDecimal.TEN, now)).isEqualTo(7L);
        repository.insertOrderItem(7L, 3L, "sku", 2, BigDecimal.ONE, now);

        verify(jdbcTemplate).update(anyString(), eq(7L), eq(3L), eq("sku"), eq(2), eq(BigDecimal.ONE), any());
    }

    @Test
    void insertOutboxBindsJsonPayload() {
        when(jdbcTemplate.queryForObject(anyString(), eq(Long.class), any(), any(), any(), any(), any(), any()))
                .thenReturn(11L);
        OutboxJdbcRepository repository = new OutboxJdbcRepository(jdbcTemplate);

        long id = repository.insert(
                Instant.parse("2026-10-13T08:00:00Z"),
                "7",
                "3",
                "OrderCreated",
                "{\"orderId\":7}",
                "00-aa"
        );

        assertThat(id).isEqualTo(11L);
        verify(jdbcTemplate).queryForObject(
                anyString(),
                eq(Long.class),
                any(),
                eq("7"),
                eq("3"),
                eq("OrderCreated"),
                eq("{\"orderId\":7}"),
                eq("00-aa")
        );
    }
}
