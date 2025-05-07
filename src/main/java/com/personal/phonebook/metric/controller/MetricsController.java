package com.personal.phonebook.metric.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.personal.phonebook.metric.service.MetricService;

@RestController
@RequestMapping("/phonebook/metrics")
public class MetricsController {

    @Autowired
    private MetricService metricService;

    @GetMapping("/contacts")
    public ResponseEntity<Map<String, Object>> getContactMetrics () {
        return ResponseEntity.ok(metricService.getContactMetrics());
    }

    @GetMapping("/performance")
    public ResponseEntity<Map<String, Object>> getPerformanceMetrics () {
        return ResponseEntity.ok(metricService.getPerformanceMetrics());
    }

    @GetMapping("/web")
    public ResponseEntity<Map<String, Object>> getWebMetrics() {
        return ResponseEntity.ok(metricService.getWebMetrics());
    }
}
