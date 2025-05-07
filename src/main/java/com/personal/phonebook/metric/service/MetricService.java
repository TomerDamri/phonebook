package com.personal.phonebook.metric.service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.search.Search;

@Service
public class MetricService {

    private static final String SEARCH_CONTACTS_TIMER = "search_contacts_timer";
    private static final String CREATE_CONTACT_TIMER = "create_contact_timer";
    private static final String UPDATE_CONTACT_TIMER = "update_contact_timer";
    private static final String DELETE_CONTACT_TIMER = "delete_contact_timer";
    private static final String SEARCH_TIME_MEAN_MS = "search_time_mean_ms";
    private static final String CREATE_TIME_MEAN_MS = "create_time_mean_ms";
    private static final String UPDATE_TIME_MEAN_MS = "update_time_mean_ms";
    private static final String DELETE_TIME_MEAN_MS = "delete_time_mean_ms";
    private static final String PHONEBOOK_CONTACTS = "phonebook.contacts";
    private static final String HTTP_REQUESTS = "http_requests";
    private static final String HTTP_SERVER_REQUESTS = "http.server.requests";
    private static final String HTTP_STATUS_PREFIX = "http.status.";
    private static final String ENDPOINT_EXECUTION_TIME = "phonebook.endpoint.execution_time";
    private static final String ENDPOINT_REQUESTS = "phonebook.endpoint.requests";

    @Autowired
    private MeterRegistry meterRegistry;

    public Map<String, Object> getContactMetrics () {
        Map<String, Object> metrics = new HashMap<>();
        // Get contact operation counts
        Search.in(meterRegistry).name(s -> s.startsWith(PHONEBOOK_CONTACTS)).meters().forEach(meter -> {
            metrics.put(meter.getId().getName(), meterRegistry.get(meter.getId().getName()).counter().count());
        });
        // Get HTTP metrics
        metrics.put(HTTP_REQUESTS, meterRegistry.get(HTTP_SERVER_REQUESTS).timer().count());
        return metrics;
    }

    public Map<String, Object> getPerformanceMetrics () {
        Map<String, Object> metrics = new HashMap<>();
        metrics.put(SEARCH_TIME_MEAN_MS, getMetricMeanTime(SEARCH_CONTACTS_TIMER));
        metrics.put(CREATE_TIME_MEAN_MS, getMetricMeanTime(CREATE_CONTACT_TIMER));
        metrics.put(UPDATE_TIME_MEAN_MS, getMetricMeanTime(UPDATE_CONTACT_TIMER));
        metrics.put(DELETE_TIME_MEAN_MS, getMetricMeanTime(DELETE_CONTACT_TIMER));
        return metrics;
    }

    public Map<String, Object> getWebMetrics () {
        Map<String, Object> metrics = new HashMap<>();

        // Collect HTTP status metrics
        Search.in(meterRegistry).name(s -> s.startsWith(HTTP_STATUS_PREFIX)).meters().forEach(meter -> {
            String statusCode = meter.getId().getName().substring(HTTP_STATUS_PREFIX.length());
            metrics.put("status_" + statusCode + "_count", meterRegistry.get(meter.getId().getName()).counter().count());
        });

        // Collect endpoint request counts
        Search.in(meterRegistry).name(ENDPOINT_REQUESTS).meters().forEach(meter -> {
            String endpoint = meter.getId().getTag("endpoint");
            if (endpoint != null) {
                metrics.put("endpoint_" + endpoint.replace("/", "_") + "_count",
                            meterRegistry.get(meter.getId().getName()).counter().count());
            }
        });

        // Collect endpoint execution times
        Search.in(meterRegistry).name(ENDPOINT_EXECUTION_TIME).meters().forEach(meter -> {
            String endpoint = meter.getId().getTag("endpoint");
            String method = meter.getId().getTag("method");
            String status = meter.getId().getTag("status");
            if (endpoint != null) {
                String key = String.format("execution_time_%s_%s_%s", endpoint.replace("/", "_"), method, status);
                metrics.put(key, meterRegistry.get(meter.getId().getName()).timer().mean(TimeUnit.MILLISECONDS));
            }
        });

        return metrics;
    }

    private double getMetricMeanTime (String metricName) {
        return meterRegistry.get(metricName).timer().mean(TimeUnit.MILLISECONDS);
    }

}
