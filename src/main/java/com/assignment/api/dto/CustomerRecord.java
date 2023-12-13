package com.assignment.api.dto;

public record CustomerRecord(
        String uuid,
        String firstName,
        String lastName,
        String street,
        String address,
        String city,
        String state,
        String email,
        String phone
) {
    // You can add additional methods or customization here if needed
}
