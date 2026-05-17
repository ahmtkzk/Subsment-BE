package com.reything.subsmentbe.controller;

import com.reything.subsmentbe.dto.auth.AuthResponse;
import com.reything.subsmentbe.dto.auth.GoogleLoginRequest;
import com.reything.subsmentbe.dto.auth.LoginRequest;
import com.reything.subsmentbe.dto.auth.RegisterRequest;
import com.reything.subsmentbe.dto.auth.TokenResponse;
import com.reything.subsmentbe.dto.auth.VerifyResponse;
import com.reything.subsmentbe.dto.common.MessageResponse;
import com.reything.subsmentbe.security.CurrentUser;
import com.reything.subsmentbe.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Auth")
public class AuthController {

    private final AuthService authService;
    private final CurrentUser currentUser;

    public AuthController(AuthService authService, CurrentUser currentUser) {
        this.authService = authService;
        this.currentUser = currentUser;
    }

    @PostMapping("/register")
    @Operation(summary = "Yeni kullanıcı kaydı")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(req));
    }

    @PostMapping("/login")
    @Operation(summary = "Giriş yap")
    public AuthResponse login(@Valid @RequestBody LoginRequest req) {
        return authService.login(req);
    }

    @PostMapping("/google")
    @Operation(summary = "Google ile giriş yap veya kaydol")
    public AuthResponse loginWithGoogle(@Valid @RequestBody GoogleLoginRequest req) {
        return authService.loginWithGoogle(req);
    }

    @PostMapping("/logout")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Çıkış yap")
    public MessageResponse logout() {
        return MessageResponse.of("Çıkış başarılı");
    }

    @PostMapping("/refresh")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Token yenile")
    public TokenResponse refresh() {
        return new TokenResponse(true, authService.refresh(currentUser.id()));
    }

    @GetMapping("/verify")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Token doğrula")
    public VerifyResponse verify() {
        currentUser.id();
        return new VerifyResponse(true, true);
    }
}
