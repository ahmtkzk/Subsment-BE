package com.reything.subsmentbe.dto.auth;

public record TokenResponse(
        boolean success,
        String token
) {
}
