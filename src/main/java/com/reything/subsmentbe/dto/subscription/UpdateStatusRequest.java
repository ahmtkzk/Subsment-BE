package com.reything.subsmentbe.dto.subscription;

import com.reything.subsmentbe.domain.enums.SubscriptionStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateStatusRequest(
        @NotNull SubscriptionStatus status
) {
}
