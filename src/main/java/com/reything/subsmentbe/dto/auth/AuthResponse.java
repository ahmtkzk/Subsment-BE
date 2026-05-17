package com.reything.subsmentbe.dto.auth;

import com.reything.subsmentbe.dto.user.UserSummary;

public record AuthResponse(
        boolean success,
        UserSummary user,
        String token,
        String message
) {
}
