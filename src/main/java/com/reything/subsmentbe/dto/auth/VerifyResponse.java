package com.reything.subsmentbe.dto.auth;

public record VerifyResponse(
        boolean success,
        boolean valid
) {
}
