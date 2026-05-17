package com.reything.subsmentbe.dto.profile;

import com.reything.subsmentbe.domain.enums.Currency;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record UpdateProfileRequest(
        Currency primaryCurrency,
        Boolean convertForeignCurrency,
        Boolean darkMode,
        Boolean notificationsEnabled,
        @Min(1) @Max(30) Integer cancelReminderDays
) {
}
