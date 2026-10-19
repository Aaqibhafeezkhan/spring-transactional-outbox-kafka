package com.kholodilin.outbox.inbound;

import com.kholodilin.outbox.events.EventEnvelope;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.header.Headers;
import org.apache.kafka.common.header.internals.RecordHeaders;
import org.apache.kafka.common.serialization.Deserializer;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * Kafka value deserializer that yields an {@link EventEnvelope} for both producer shapes.
 * <p>
 * Kafka calls {@link #deserialize(String, Headers, byte[])}. The bytes are read into a
 * {@link JsonNode} before any parser runs. {@code application.yml} does not choose the parser.
 */
public final class EventEnvelopeDeserializer implements Deserializer<EventEnvelope> {

    private final JsonMapper objectMapper;
    private final InboundMessageParserRegistry registry;

    /**
     * @param objectMapper mapper from {@link com.kholodilin.outbox.config.KafkaConsumerConfig#kafkaObjectMapper()}
     * @param registry     header routing for servlet envelopes and Debezium order payloads
     */
    public EventEnvelopeDeserializer(JsonMapper objectMapper, InboundMessageParserRegistry registry) {
        this.objectMapper = objectMapper;
        this.registry = registry;
    }

    /**
     * Header-less entry point. No {@code messageType} is available, so the servlet parser runs.
     */
    @Override
    public EventEnvelope deserialize(String topic, byte[] data) {
        return deserialize(topic, new RecordHeaders(), data);
    }

    @Override
    public EventEnvelope deserialize(String topic, Headers headers, byte[] data) {
        if (data == null) {
            return null;
        }
        final JsonNode body;
        try {
            body = objectMapper.readTree(data);
        } catch (JacksonException ex) {
            throw new SerializationException("Failed to read Kafka record value as JSON", ex);
        }
        return registry.parse(body, headers);
    }
}
