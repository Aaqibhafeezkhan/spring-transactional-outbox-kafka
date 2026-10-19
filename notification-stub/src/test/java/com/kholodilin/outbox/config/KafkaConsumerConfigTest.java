package com.kholodilin.outbox.config;

import com.kholodilin.outbox.events.EventEnvelope;
import com.kholodilin.outbox.inbound.EventEnvelopeDeserializer;
import org.apache.kafka.common.header.internals.RecordHeaders;
import org.apache.kafka.common.serialization.Deserializer;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JacksonJsonDeserializer;
import org.springframework.mock.env.MockEnvironment;
import tools.jackson.databind.json.JsonMapper;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class KafkaConsumerConfigTest {

    @Test
    void deserializesInstantFromIso8601() throws Exception {
        JsonMapper mapper = KafkaConsumerConfig.kafkaObjectMapper();
        String json = """
                {
                  "eventId": 1,
                  "orderId": 10,
                  "customerId": 20,
                  "eventType": "OrderCreated",
                  "payload": {"orderId": 10},
                  "occurredAt": "2026-07-12T10:15:30Z"
                }
                """;

        EventEnvelope envelope = mapper.readValue(json, EventEnvelope.class);

        assertThat(envelope.eventId()).isEqualTo(1L);
        assertThat(envelope.occurredAt()).isEqualTo(Instant.parse("2026-07-12T10:15:30Z"));
        assertThat(envelope.payload()).containsEntry("orderId", 10);
    }

    @Test
    void consumerFactoryUsesEventEnvelopeDeserializer() {
        MockEnvironment environment = new MockEnvironment()
                .withProperty("spring.kafka.bootstrap-servers", "localhost:9092")
                .withProperty("spring.kafka.consumer.group-id", "notification-stub");
        KafkaConsumerConfig config = new KafkaConsumerConfig();

        ConsumerFactory<String, EventEnvelope> factory = config.consumerFactory(
                NotificationStubProperties.builder().build(),
                environment
        );

        assertThat(factory).isInstanceOf(DefaultKafkaConsumerFactory.class);
        Deserializer<?> valueDeserializer = ((DefaultKafkaConsumerFactory<String, EventEnvelope>) factory)
                .getValueDeserializer();
        assertThat(valueDeserializer)
                .isInstanceOf(EventEnvelopeDeserializer.class)
                .isNotInstanceOf(JacksonJsonDeserializer.class);

        String json = """
                {
                  "eventId": 1,
                  "orderId": 10,
                  "customerId": 20,
                  "eventType": "OrderCreated",
                  "payload": {"orderId": 10},
                  "occurredAt": "2026-07-12T10:15:30Z"
                }
                """;
        EventEnvelope envelope = ((EventEnvelopeDeserializer) valueDeserializer).deserialize(
                "orders.events",
                new RecordHeaders(),
                json.getBytes(StandardCharsets.UTF_8)
        );
        assertThat(envelope.eventId()).isEqualTo(1L);

        ConcurrentKafkaListenerContainerFactory<String, EventEnvelope> listenerFactory =
                config.batchKafkaListenerContainerFactory(factory, NotificationStubProperties.builder().build());
        assertThat(listenerFactory.isBatchListener()).isTrue();
    }
}
