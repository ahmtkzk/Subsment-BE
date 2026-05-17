package com.reything.subsmentbe.dto.profile;

import com.reything.subsmentbe.domain.enums.Currency;

import java.time.OffsetDateTime;
import java.util.UUID;

public record ProfileResponse(
        UUID id,
        UUID userId,
        Currency primaryCurrency,
        Boolean convertForeignCurrency,
        Boolean darkMode,
        Boolean notificationsEnabled,
        Integer cancelReminderDays,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}
