package com.pabloverdejo.stockpilot.common;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;

class RequestTraceFilterTest {

    @Test
    void echoesAndLogsTheRequestIdThenClearsDiagnosticContext() throws Exception {
        var logger = (Logger) LoggerFactory.getLogger(RequestTraceFilter.class);
        var appender = new ListAppender<ILoggingEvent>();
        appender.start();
        logger.addAppender(appender);

        try {
            var request = new MockHttpServletRequest("DELETE", "/api/v1/products/product-id");
            request.addHeader(RequestTraceFilter.HEADER, "trace-test-123");
            var response = new MockHttpServletResponse();

            new RequestTraceFilter().doFilter(request, response, new MockFilterChain());

            assertThat(response.getHeader(RequestTraceFilter.HEADER)).isEqualTo("trace-test-123");
            assertThat(appender.list)
                    .extracting(ILoggingEvent::getFormattedMessage)
                    .anySatisfy(message -> assertThat(message)
                            .contains("requestId=trace-test-123")
                            .contains("method=DELETE")
                            .contains("path=/api/v1/products/product-id")
                            .contains("status=200"));
            assertThat(MDC.get("requestId")).isNull();
        } finally {
            logger.detachAppender(appender);
            appender.stop();
        }
    }
}
