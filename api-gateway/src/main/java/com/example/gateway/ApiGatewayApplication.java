package com.example.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@SpringBootApplication
@RestController
public class ApiGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
    }

    @GetMapping("/")
    public ResponseEntity<Map<String, Object>> rootIndex() {
        return ResponseEntity.ok(Map.of(
                "service", "Spring Cloud API Gateway",
                "version", "Spring Boot 4.x + Spring Cloud 2025.x",
                "eurekaDashboard", "http://localhost:8761",
                "availableEndpoints", List.of(
                        Map.of(
                                "method", "GET",
                                "path", "/api/customers",
                                "target", "customer-service via Eureka (lb://customer-service)",
                                "description", "List all customers"
                        ),
                        Map.of(
                                "method", "GET",
                                "path", "/api/customers/1",
                                "target", "customer-service via Eureka (lb://customer-service)",
                                "description", "Get customer by ID"
                        ),
                        Map.of(
                                "method", "GET",
                                "path", "/api/customers/info",
                                "target", "customer-service via Eureka",
                                "description", "Instance metadata (demonstrates load balancing)"
                        ),
                        Map.of(
                                "method", "GET",
                                "path", "/api/orders",
                                "target", "order-service via Eureka (lb://order-service)",
                                "description", "List all orders"
                        ),
                        Map.of(
                                "method", "GET",
                                "path", "/api/orders/101",
                                "target", "order-service via Eureka (lb://order-service)",
                                "description", "Get order enriched with customer data via inter-service discovery call"
                        ),
                        Map.of(
                                "method", "GET",
                                "path", "/api/orders/info",
                                "target", "order-service via Eureka",
                                "description", "Instance metadata"
                        )
                )
        ));
    }
}
