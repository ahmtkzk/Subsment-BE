package com.reything.subsmentbe.security;

import com.reything.subsmentbe.exception.ApiException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class CurrentUser {

    public UUID id() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof UUID uuid)) {
            throw ApiException.unauthorized("Kimlik doğrulanmadı");
        }
        return uuid;
    }
}
