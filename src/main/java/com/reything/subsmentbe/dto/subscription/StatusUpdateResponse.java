package com.reything.subsmentbe.dto.subscription;

import com.reything.subsmentbe.domain.enums.SubscriptionStatus;

import java.time.OffsetDateTime;
import java.util.UUID;

public record StatusUpdateResponse(
        UUID id,
        SubscriptionStatus status,
        OffsetDateTime updatedAt
) {
}
