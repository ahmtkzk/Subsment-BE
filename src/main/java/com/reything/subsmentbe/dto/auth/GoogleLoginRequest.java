package com.reything.subsmentbe.dto.auth;

import jakarta.validation.constraints.NotBlank;

public record GoogleLoginRequest(
        @NotBlank(message = "ID Token boş olamaz")
        String idToken
) {
}
