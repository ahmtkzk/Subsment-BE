package com.reything.subsmentbe.dto.user;

import java.util.UUID;

public record UserSummary(
        UUID id,
        String name,
        String email
) {
}
