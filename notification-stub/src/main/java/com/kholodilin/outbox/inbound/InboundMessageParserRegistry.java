package com.kholodilin.outbox.inbound;

import com.kholodilin.outbox.events.EventEnvelope;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.header.Headers;
import tools.jackson.databind.JsonNode;

/**
 * Picks the inbound parser from {@code messageType} and {@code messageVersion}.
 * <p>
 * No {@code messageType} keeps the servlet envelope path. {@code order-payload} version {@code 1}
 * is the Debezium domain payload. Any other pair fails the record.
 */
public final class InboundMessageParserRegistry {

    static final String HEADER_MESSAGE_TYPE = "messageType";
    static final String HEADER_MESSAGE_VERSION = "messageVersion";
    static final String ORDER_PAYLOAD = "order-payload";
    static final String ORDER_PAYLOAD_VERSION = "1";

    private final InboundMessageParser servletEnvelopeParser;
    private final InboundMessageParser orderPayloadParser;

    /**
     * @param servletEnvelopeParser parser used when {@code messageType} is absent
     * @param orderPayloadParser    parser used for {@code order-payload} version {@code 1}
     */
    public InboundMessageParserRegistry(
            InboundMessageParser servletEnvelopeParser,
            InboundMessageParser orderPayloadParser
    ) {
        this.servletEnvelopeParser = servletEnvelopeParser;
        this.orderPayloadParser = orderPayloadParser;
    }

    /**
     * Routes one record. This is the only place that branches on message type and version.
     *
     * @param body    JSON value read from the record bytes
     * @param headers Kafka record headers
     * @return envelope for the notification handler
     */
    public EventEnvelope parse(JsonNode body, Headers headers) {
        String messageType = HeaderCodec.text(headers, HEADER_MESSAGE_TYPE);
        if (messageType == null) {
            return servletEnvelopeParser.parse(body, headers);
        }
        String messageVersion = HeaderCodec.text(headers, HEADER_MESSAGE_VERSION);
        if (ORDER_PAYLOAD.equals(messageType) && ORDER_PAYLOAD_VERSION.equals(messageVersion)) {
            return orderPayloadParser.parse(body, headers);
        }
        throw new SerializationException(
                "Unsupported Kafka message messageType=" + messageType + " messageVersion=" + messageVersion
        );
    }
}
