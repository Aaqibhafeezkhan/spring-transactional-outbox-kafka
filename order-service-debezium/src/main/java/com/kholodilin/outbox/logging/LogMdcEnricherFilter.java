package com.kholodilin.outbox.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/** Adds instance and tracing alias fields to MDC for every HTTP request. */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class LogMdcEnricherFilter extends OncePerRequestFilter {

    private final String instanceId;

    public LogMdcEnricherFilter(@Value("${app.instance-id:local}") String instanceId) {
        this.instanceId = instanceId;
    }

    /**
     * Puts instance id and tracing aliases into MDC for the duration of the request.
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        StructuredLogContext.putInstanceFields(instanceId);
        StructuredLogContext.enrichTracingAliases();
        try {
            filterChain.doFilter(request, response);
        } finally {
            StructuredLogContext.enrichTracingAliases();
        }
    }
}
