package com.example.order;

public record CustomerDto(
        Long id,
        String name,
        String email,
        String tier
) {
}
