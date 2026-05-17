package com.reything.subsmentbe.dto.common;

public record ErrorDetail(
        String code,
        String message,
        Object details
) {
}
