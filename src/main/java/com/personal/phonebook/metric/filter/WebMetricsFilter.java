package com.personal.phonebook.metric.filter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class WebMetricsFilter extends OncePerRequestFilter {

    private static final String PHONEBOOK_ENDPOINT_EXECUTION_TIME = "phonebook.endpoint.execution_time";
    private static final String PHONEBOOK_API_PREFIX = "/phonebook";
    private static final String ENDPOINT = "endpoint";
    private static final String METHOD = "method";
    private static final String STATUS = "status";
    private static final String PHONEBOOK_ENDPOINT_REQUESTS = "phonebook.endpoint.requests";
    private static final String ENDPOINT_COUNTER_DESCRIPTION = "Requests to specific endpoints";
    private final MeterRegistry meterRegistry;
    private final Map<Integer, Counter> statusCounters = new ConcurrentHashMap<>();
    private final Map<String, Counter> endpointCounters = new ConcurrentHashMap<>();

    @Autowired
    public WebMetricsFilter (MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    @Override
    protected void doFilterInternal (HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
                throws ServletException, IOException {
        long startTime = System.currentTimeMillis();

        try {
            filterChain.doFilter(request, response);
        }
        finally {
            recordMetrics(request, response, System.currentTimeMillis() - startTime);
        }
    }

    private void recordMetrics (HttpServletRequest request, HttpServletResponse response, long executionTime) {
        String endpoint = request.getRequestURI();
        int status = response.getStatus();

        // Record status code metrics
        statusCounters.computeIfAbsent(status, this::createStatusCounter).increment();

        // Record endpoint metrics
        if (endpoint.startsWith(PHONEBOOK_API_PREFIX)) {
            endpointCounters.computeIfAbsent(endpoint, this::createEndpointCounter).increment();

            // Record execution time
            meterRegistry.timer(PHONEBOOK_ENDPOINT_EXECUTION_TIME,
                                ENDPOINT,
                                endpoint,
                                METHOD,
                                request.getMethod(),
                                STATUS,
                                String.valueOf(status))
                         .record(executionTime, java.util.concurrent.TimeUnit.MILLISECONDS);
        }
    }

    private Counter createStatusCounter (int status) {
        return Counter.builder("http.status." + status).description("HTTP " + status + " responses").register(meterRegistry);
    }

    private Counter createEndpointCounter (String endpoint) {
        return Counter.builder(PHONEBOOK_ENDPOINT_REQUESTS)
                      .description(ENDPOINT_COUNTER_DESCRIPTION)
                      .tag(ENDPOINT, endpoint)
                      .register(meterRegistry);
    }
}
