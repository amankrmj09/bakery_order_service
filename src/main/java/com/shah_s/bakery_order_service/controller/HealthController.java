package com.shah_s.bakery_order_service.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.sql.DataSource;
import java.sql.Connection;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")

public class HealthController {

    final private DataSource dataSource;

    public HealthController(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    // Main service health check
    @GetMapping("/health")
    public ResponseEntity<org.devofblue.common.dto.HealthResponseDto> health() {
        org.devofblue.common.dto.HealthResponseDto response = new org.devofblue.common.dto.HealthResponseDto("UP", "bakery-order-service");
        Map<String, Object> details = new HashMap<>();
        details.put("version", "1.0.0");

        // Check database connectivity
        try (Connection connection = dataSource.getConnection()) {
            details.put("database", "UP");
            details.put("databaseUrl", connection.getMetaData().getURL());
        } catch (Exception e) {
            details.put("database", "DOWN");
            details.put("databaseError", e.getMessage());
        }

        response.setDetails(details);
        return ResponseEntity.ok(response);
    }

    // Service info
    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> info() {
        Map<String, Object> response = new HashMap<>();
        response.put("serviceName", "Bakery Order Service");
        response.put("description", "Order management and payment processing service");
        response.put("version", "1.0.0");
        response.put("features", Map.of(
            "orders", "Complete order lifecycle management",
            "payments", "Multi-method payment processing",
            "integration", "Product service integration for stock management",
            "analytics", "Order and payment analytics"
        ));
        response.put("endpoints", Map.of(
            "orders", "/api/orders",
            "payments", "/api/payments"
        ));

        return ResponseEntity.ok(response);
    }

    // Service metrics
    @GetMapping("/metrics")
    public ResponseEntity<Map<String, Object>> metrics() {
        Map<String, Object> response = new HashMap<>();
        response.put("uptime", getUptime());
        response.put("timestamp", LocalDateTime.now().toString());

        // Memory info
        Runtime runtime = Runtime.getRuntime();
        Map<String, Object> memory = new HashMap<>();
        memory.put("maxMemory", runtime.maxMemory() / 1024 / 1024 + " MB");
        memory.put("totalMemory", runtime.totalMemory() / 1024 / 1024 + " MB");
        memory.put("freeMemory", runtime.freeMemory() / 1024 / 1024 + " MB");
        memory.put("usedMemory", (runtime.totalMemory() - runtime.freeMemory()) / 1024 / 1024 + " MB");
        response.put("memory", memory);

        return ResponseEntity.ok(response);
    }

    private String getUptime() {
        long uptime = java.lang.management.ManagementFactory.getRuntimeMXBean().getUptime();
        long seconds = uptime / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;

        return String.format("%d days, %d hours, %d minutes, %d seconds",
                days, hours % 24, minutes % 60, seconds % 60);
    }
}
