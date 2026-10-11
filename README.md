# Spring Boot 4 Microservices with Docker, Eureka, and API Gateway

A complete reference example demonstrating **Spring Boot 4.x**, **Spring Cloud (2025.x release train)**, **Netflix Eureka Service Registry**, **Spring Cloud Gateway**, and **Docker Compose**.

---

## 🏛️ System Architecture

```mermaid
flowchart TD
    Client(["Client / Browser / Postman"])

    subgraph DockerNetwork ["Docker Compose Bridge Network (spring-cloud-net)"]
        Gateway["API Gateway (Port 8080)<br/>Spring Cloud Gateway Server WebMvc"]
        Eureka["Eureka Server (Port 8761)<br/>Service Registry & Discovery"]
        
        CustomerSvc["Customer Service (Port 8081)<br/>Microservice 1"]
        OrderSvc["Order Service (Port 8082)<br/>Microservice 2"]
    end

    Client -->|HTTP /api/...| Gateway
    Gateway -->|Register & Fetch Registry| Eureka
    CustomerSvc -->|Register with Heartbeat| Eureka
    OrderSvc -->|Register with Heartbeat| Eureka

    Gateway -->|lb://customer-service| CustomerSvc
    Gateway -->|lb://order-service| OrderSvc
    
    OrderSvc -.->|Inter-service call<br/>http://customer-service/api/customers/{id}| CustomerSvc
```

---

## 📦 Components

| Service | Port | Description | Technologies |
|---|---|---|---|
| **`eureka-server`** | `8761` | Service discovery registry dashboard and peer discovery | Spring Boot 4, Spring Cloud Netflix Eureka Server, Actuator |
| **`api-gateway`** | `8080` | Unified edge router, path predicate dispatching, and dynamic service load-balancing | Spring Cloud Gateway Server WebMvc, Eureka Client, LoadBalancer |
| **`customer-service`** | `8081` | Manages customer data and tier metadata | Spring Boot 4 (`webmvc`), Eureka Client, Actuator |
| **`order-service`** | `8082` | Manages orders and demonstrates **inter-service communication** via Eureka using `@LoadBalanced RestClient` | Spring Boot 4 (`webmvc`), Eureka Client, LoadBalancer, Actuator |

---

## 🚀 How to Run with Docker Compose

### 1. Build and Start All Services
Using the included Maven wrapper and Docker Compose:

**On Windows:**
```cmd
mvnw.cmd clean package -DskipTests
docker compose up --build
```
*(Or simply double-click / run `build-and-run.bat`)*

**On Linux / macOS / WSL:**
```bash
./mvnw clean package -DskipTests
docker compose up --build
```
*(Or run `./build-and-run.sh`)*

### 2. Verify Services Startup
Docker Compose starts services in dependency order:
1. `eureka-server` boots up and passes the `/actuator/health` check.
2. `customer-service` and `order-service` boot up and register with Eureka.
3. `api-gateway` starts and dynamically loads registered routes.

---

## 🧪 Testing the Microservices

### 1. Inspect the Eureka Registry Dashboard
Open your browser at:
👉 **[http://localhost:8761](http://localhost:8761)**

You will see:
* `API-GATEWAY`
* `CUSTOMER-SERVICE`
* `ORDER-SERVICE`
all registered with `UP` status.

---

### 2. API Gateway Navigation Guide
The Gateway provides an index endpoint at root:
```bash
curl http://localhost:8080/
```

---

### 3. Test Routing to Customer Service via Gateway
Query customer endpoints through Gateway (`port 8080`):

```bash
# Get all customers
curl http://localhost:8080/api/customers

# Get customer by ID
curl http://localhost:8080/api/customers/1

# Inspect instance metadata (demonstrates instance details)
curl http://localhost:8080/api/customers/info
```

**Sample Output (`GET /api/customers/1`):**
```json
{
  "id": 1,
  "name": "Alice Johnson",
  "email": "alice@example.com",
  "tier": "PLATINUM"
}
```

---

### 4. Test Inter-Service Communication via Order Service
When you request an order, the Gateway routes to `order-service`, and `order-service` automatically discovers `customer-service` through Eureka using `@LoadBalanced RestClient` to fetch and embed the customer record:

```bash
# Get all orders
curl http://localhost:8080/api/orders

# Get order #101 (enriched with Alice Johnson's customer details)
curl http://localhost:8080/api/orders/101
```

**Sample Output (`GET /api/orders/101`):**
```json
{
  "id": 101,
  "product": "Cloud Native Architecture Book",
  "amount": 49.99,
  "status": "CONFIRMED",
  "customer": {
    "id": 1,
    "name": "Alice Johnson",
    "email": "alice@example.com",
    "tier": "PLATINUM"
  },
  "note": "Customer retrieved via Eureka discovery from customer-service"
}
```

---

## 🔍 Key Architectural Details

1. **Spring Boot 4 Baseline:**
   Uses Java 21+ and the new `spring-boot-starter-webmvc` starter, modernizing the web layer with Jakarta EE 11 support and virtual threads.

2. **Spring Cloud Gateway Server WebMvc:**
   Routes incoming traffic dynamically using the `lb://<service-name>` prefix defined in `application.yml`:
   ```yaml
   spring:
     cloud:
       gateway:
         server:
           webmvc:
             routes:
               - id: customer-service-route
                 uri: lb://customer-service
                 predicates:
                   - Path=/api/customers/**
   ```

3. **Inter-Service Service Discovery:**
   `order-service` injects Spring Cloud's `LoadBalancerClient` to dynamically resolve instances of `customer-service`:
   ```java
   ServiceInstance instance = loadBalancerClient.choose("customer-service");
   String targetUrl = instance.getUri().toString() + "/api/customers/" + order.customerId();
   CustomerDto customer = restClient.get().uri(targetUrl).retrieve().body(CustomerDto.class);
   ```
   Calling `customer-service` resolves the IP and port dynamically through Eureka without hardcoded URLs or static ports.

4. **Docker Security & Performance:**
   - Multi-stage / lightweight runtime using `eclipse-temurin:21-jre-alpine`.
   - Runs as non-root user (`USER spring:spring`).
   - JVM container-aware memory constraints (`-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0`).
   - Docker health checks on Actuator endpoints ensuring ordered startup.
