package com.kholodilin.outbox.inbound;

import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.Headers;

import java.nio.charset.StandardCharsets;

/** Reads the last Kafka header value as UTF-8 text. */
interface HeaderCodec {

    /**
     * @param headers record headers, or {@code null} when the caller has none
     * @param name    header name
     * @return UTF-8 text, or {@code null} when the header or its value is absent
     */
    static String text(Headers headers, String name) {
        if (headers == null) {
            return null;
        }
        Header header = headers.lastHeader(name);
        if (header == null || header.value() == null) {
            return null;
        }
        return new String(header.value(), StandardCharsets.UTF_8);
    }
}
