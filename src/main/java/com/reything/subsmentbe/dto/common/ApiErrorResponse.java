package com.reything.subsmentbe.dto.common;

import java.time.OffsetDateTime;

public record ApiErrorResponse(
        boolean success,
        ErrorDetail error,
        OffsetDateTime timestamp
) {
}
