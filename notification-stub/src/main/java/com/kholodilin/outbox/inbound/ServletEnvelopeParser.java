package com.kholodilin.outbox.inbound;

import com.kholodilin.outbox.events.EventEnvelope;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.header.Headers;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * Maps a servlet, virtual-thread, or reactive {@link EventEnvelope} JSON body.
 * <p>
 * Those producers do not send {@code messageType}. The JSON shape stays the one they publish today.
 */
public final class ServletEnvelopeParser implements InboundMessageParser {

    private final JsonMapper objectMapper;

    /**
     * @param objectMapper mapper from {@link com.kholodilin.outbox.config.KafkaConsumerConfig#kafkaObjectMapper()}
     */
    public ServletEnvelopeParser(JsonMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public EventEnvelope parse(JsonNode body, Headers headers) {
        try {
            return objectMapper.treeToValue(body, EventEnvelope.class);
        } catch (JacksonException ex) {
            throw new SerializationException("Failed to map Kafka value to EventEnvelope", ex);
        }
    }
}
