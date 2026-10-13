package com.kholodilin.outbox;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/** Order API that persists the transactional outbox for a later Debezium publisher. */
@SpringBootApplication
public class OrderServiceDebeziumApplication {

    /**
     * Boots the Debezium write-path (API, idempotency, outbox insert). No Kafka publisher.
     *
     * @param args standard Spring Boot arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(OrderServiceDebeziumApplication.class, args);
    }
}
