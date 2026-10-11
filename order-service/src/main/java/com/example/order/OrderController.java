package com.example.order;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private static final Logger log = LoggerFactory.getLogger(OrderController.class);

    @Value("${server.port:8082}")
    private String serverPort;

    @Value("${eureka.instance.instance-id:${spring.application.name}}")
    private String instanceId;

    private final RestClient restClient;
    private final LoadBalancerClient loadBalancerClient;
    private final Map<Long, Order> orderDatabase = new ConcurrentHashMap<>();

    public OrderController(LoadBalancerClient loadBalancerClient) {
        this.restClient = RestClient.builder().build();
        this.loadBalancerClient = loadBalancerClient;

        orderDatabase.put(101L, new Order(101L, 1L, "Cloud Native Architecture Book", 49.99, "CONFIRMED"));
        orderDatabase.put(102L, new Order(102L, 2L, "Kubernetes and Docker Guide", 39.50, "SHIPPED"));
        orderDatabase.put(103L, new Order(103L, 1L, "Spring Boot Microservices Course", 99.00, "PROCESSING"));
        orderDatabase.put(104L, new Order(104L, 3L, "Wireless Mechanical Keyboard", 129.99, "DELIVERED"));
    }

    @GetMapping
    public List<Order> getAllOrders() {
        return List.copyOf(orderDatabase.values());
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderDetails> getOrderById(@PathVariable Long id) {
        Order order = orderDatabase.get(id);
        if (order == null) {
            return ResponseEntity.notFound().build();
        }

        CustomerDto customer = null;
        String note;

        try {
            // Dynamically discover and load-balance customer-service from Eureka
            ServiceInstance instance = loadBalancerClient.choose("customer-service");
            if (instance != null) {
                String targetUrl = instance.getUri().toString() + "/api/customers/" + order.customerId();
                log.info("Discovered customer-service instance: {} ({})", instance.getInstanceId(), instance.getUri());
                customer = restClient.get()
                        .uri(targetUrl)
                        .retrieve()
                        .body(CustomerDto.class);
                note = "Customer retrieved via Eureka discovery from " + instance.getUri();
            } else {
                note = "No customer-service instances available in Eureka registry";
                log.warn(note);
            }
        } catch (Exception e) {
            log.warn("Could not fetch customer details for customer ID {}: {}", order.customerId(), e.getMessage());
            note = "Customer service call failed or unavailable (" + e.getMessage() + ")";
        }

        OrderDetails details = new OrderDetails(
                order.id(),
                order.product(),
                order.amount(),
                order.status(),
                customer,
                note
        );

        return ResponseEntity.ok(details);
    }

    @GetMapping("/info")
    public Map<String, Object> getInstanceInfo() {
        return Map.of(
                "service", "order-service",
                "port", serverPort,
                "instanceId", instanceId,
                "timestamp", System.currentTimeMillis()
        );
    }
}
