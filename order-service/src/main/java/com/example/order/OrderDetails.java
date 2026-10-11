package com.example.order;

public record OrderDetails(
        Long id,
        String product,
        Double amount,
        String status,
        CustomerDto customer,
        String note
) {
}
