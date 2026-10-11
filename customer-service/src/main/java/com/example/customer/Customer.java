package com.example.customer;

public record Customer(
        Long id,
        String name,
        String email,
        String tier
) {
}
