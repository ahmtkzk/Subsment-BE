package com.reything.subsmentbe.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public record UpdateUserRequest(
        String name,
        @Email String email,
        String oldPassword,
        @Size(min = 8) String password
) {
}
