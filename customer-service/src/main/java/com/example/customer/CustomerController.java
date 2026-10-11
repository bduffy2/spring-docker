package com.example.customer;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {

    @Value("${server.port:8081}")
    private String serverPort;

    @Value("${eureka.instance.instance-id:${spring.application.name}}")
    private String instanceId;

    private final Map<Long, Customer> customerDatabase = new ConcurrentHashMap<>();

    public CustomerController() {
        customerDatabase.put(1L, new Customer(1L, "Alice Johnson", "alice@example.com", "PLATINUM"));
        customerDatabase.put(2L, new Customer(2L, "Bob Smith", "bob@example.com", "GOLD"));
        customerDatabase.put(3L, new Customer(3L, "Charlie Brown", "charlie@example.com", "SILVER"));
    }

    @GetMapping
    public List<Customer> getAllCustomers() {
        return List.copyOf(customerDatabase.values());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Customer> getCustomerById(@PathVariable Long id) {
        Customer customer = customerDatabase.get(id);
        if (customer != null) {
            return ResponseEntity.ok(customer);
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/info")
    public Map<String, Object> getInstanceInfo() {
        return Map.of(
                "service", "customer-service",
                "port", serverPort,
                "instanceId", instanceId,
                "timestamp", System.currentTimeMillis()
        );
    }
}
