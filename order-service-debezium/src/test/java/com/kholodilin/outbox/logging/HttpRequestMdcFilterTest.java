package com.kholodilin.outbox.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class HttpRequestMdcFilterTest {

    private final HttpRequestMdcFilter filter = new HttpRequestMdcFilter();

    @AfterEach
    void tearDown() {
        MDC.clear();
    }

    @Test
    void clearsRequestMdcAfterOrderPost() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/v1/orders");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        MDC.put("correlationId", "corr-1");
        MDC.put("customerId", "42");

        filter.doFilter(request, response, chain);

        verify(chain).doFilter(request, response);
        assertThat(MDC.get("correlationId")).isNull();
        assertThat(MDC.get("customerId")).isNull();
        assertThat(filter.shouldNotFilter(request)).isFalse();
    }

    @Test
    void doesNotFilterOtherPaths() {
        assertThat(filter.shouldNotFilter(new MockHttpServletRequest("GET", "/actuator/health"))).isTrue();
        assertThat(filter.shouldNotFilter(new MockHttpServletRequest("POST", "/api/v1/other"))).isTrue();
    }

    @Test
    void clearsMdcWhenChainFails() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/v1/orders");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);
        doThrow(new ServletException("boom")).when(chain).doFilter(request, response);
        MDC.put("orderId", "1");

        assertThatThrownBy(() -> filter.doFilter(request, response, chain))
                .isInstanceOf(ServletException.class);
        assertThat(MDC.get("orderId")).isNull();
    }
}
