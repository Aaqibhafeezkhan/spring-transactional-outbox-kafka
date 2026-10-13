package com.kholodilin.outbox.api;

import com.kholodilin.outbox.events.EventConstants;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
@ActiveProfiles("test")
class OrderApiIT {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void newKeyInsertsOneOutboxRow() {
        long customerId = 71L;
        String key = UUID.randomUUID().toString();
        ResponseEntity<Map> created = post(customerId, key, "sku-1");

        assertThat(created.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(created.getBody()).containsKeys("orderId", "eventId");
        assertThat(countOrders(customerId)).isEqualTo(1L);
        assertThat(countOutbox(customerId)).isEqualTo(1L);

        Map<String, Object> row = jdbcTemplate.queryForMap(
                """
                        SELECT aggregate_id, partition_key, event_type, payload::text AS payload, trace_parent
                        FROM outbox_events
                        WHERE partition_key = ?
                        """,
                String.valueOf(customerId)
        );
        assertThat(row.get("event_type")).isEqualTo("OrderCreated");
        assertThat(row.get("partition_key")).isEqualTo(String.valueOf(customerId));
        assertThat(row.get("aggregate_id")).isEqualTo(String.valueOf(created.getBody().get("orderId")));
        assertThat((String) row.get("payload")).contains("sku-1");
        assertThat(row).containsKey("trace_parent");
    }

    @Test
    void sameKeyAndBodyReplaysWithoutSecondRow() {
        long customerId = 72L;
        String key = UUID.randomUUID().toString();
        ResponseEntity<Map> first = post(customerId, key, "sku-1");
        ResponseEntity<Map> second = post(customerId, key, "sku-1");

        assertThat(first.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(second.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(second.getBody().get("orderId")).isEqualTo(first.getBody().get("orderId"));
        assertThat(countOrders(customerId)).isEqualTo(1L);
        assertThat(countOutbox(customerId)).isEqualTo(1L);
    }

    @Test
    void sameKeyDifferentBodyReturns409() {
        long customerId = 73L;
        String key = UUID.randomUUID().toString();
        post(customerId, key, "sku-1");

        ResponseEntity<ProblemDetail> conflict = restTemplate.postForEntity(
                "/api/v1/orders",
                new HttpEntity<>(body(customerId, "sku-2"), jsonHeaders(key)),
                ProblemDetail.class
        );

        assertThat(conflict.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(countOrders(customerId)).isEqualTo(1L);
        assertThat(countOutbox(customerId)).isEqualTo(1L);
    }

    private ResponseEntity<Map> post(long customerId, String key, String productId) {
        return restTemplate.postForEntity(
                "/api/v1/orders",
                new HttpEntity<>(body(customerId, productId), jsonHeaders(key)),
                Map.class
        );
    }

    private static HttpHeaders jsonHeaders(String key) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set(EventConstants.IDEMPOTENCY_KEY_HEADER, key);
        return headers;
    }

    private static Map<String, Object> body(long customerId, String productId) {
        return Map.of(
                "customerId", customerId,
                "items", List.of(Map.of("productId", productId, "quantity", 1, "price", 5.00))
        );
    }

    private long countOrders(long customerId) {
        Long count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM orders WHERE customer_id = ?",
                Long.class,
                customerId
        );
        return count == null ? 0L : count;
    }

    private long countOutbox(long customerId) {
        Long count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM outbox_events WHERE partition_key = ?",
                Long.class,
                String.valueOf(customerId)
        );
        return count == null ? 0L : count;
    }
}
