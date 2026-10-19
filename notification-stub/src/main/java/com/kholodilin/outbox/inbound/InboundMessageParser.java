package com.kholodilin.outbox.inbound;

import com.kholodilin.outbox.events.EventEnvelope;
import org.apache.kafka.common.header.Headers;
import tools.jackson.databind.JsonNode;

/**
 * Turns one Kafka record body into an {@link EventEnvelope}.
 * <p>
 * {@link InboundMessageParserRegistry} chooses the implementation. A parser does not decide
 * whether it applies.
 */
public interface InboundMessageParser {

    /**
     * Maps an already-parsed JSON body to the envelope the handler and idempotency key expect.
     *
     * @param body    JSON value read from the record bytes
     * @param headers Kafka record headers
     * @return envelope for {@code NotificationStubHandler}
     */
    EventEnvelope parse(JsonNode body, Headers headers);
}
