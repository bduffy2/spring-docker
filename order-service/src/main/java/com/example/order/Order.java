package com.example.order;

public record Order(
        Long id,
        Long customerId,
        String product,
        Double amount,
        String status
) {
}
