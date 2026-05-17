package com.reything.subsmentbe.controller;

import com.reything.subsmentbe.dto.common.MessageResponse;
import com.reything.subsmentbe.dto.user.UpdateUserRequest;
import com.reything.subsmentbe.dto.user.UserResponse;
import com.reything.subsmentbe.security.CurrentUser;
import com.reything.subsmentbe.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@Tag(name = "User")
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    private final UserService userService;
    private final CurrentUser currentUser;

    public UserController(UserService userService, CurrentUser currentUser) {
        this.userService = userService;
        this.currentUser = currentUser;
    }

    @GetMapping("/me")
    @Operation(summary = "Profil bilgisi")
    public UserResponse me() {
        return userService.getMe(currentUser.id());
    }

    @PutMapping("/me")
    @Operation(summary = "Profil güncelle")
    public UserResponse updateMe(@Valid @RequestBody UpdateUserRequest req) {
        return userService.updateMe(currentUser.id(), req);
    }

    @DeleteMapping("/me")
    @Operation(summary = "Hesabı sil")
    public MessageResponse deleteMe() {
        userService.deleteMe(currentUser.id());
        return MessageResponse.of("Hesap silindi");
    }
}
