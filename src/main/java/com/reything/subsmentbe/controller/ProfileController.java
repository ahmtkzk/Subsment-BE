package com.reything.subsmentbe.controller;

import com.reything.subsmentbe.dto.profile.ProfileResponse;
import com.reything.subsmentbe.dto.profile.UpdateProfileRequest;
import com.reything.subsmentbe.security.CurrentUser;
import com.reything.subsmentbe.service.ProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/profile")
@Tag(name = "Profile")
@SecurityRequirement(name = "bearerAuth")
public class ProfileController {

    private final ProfileService profileService;
    private final CurrentUser currentUser;

    public ProfileController(ProfileService profileService, CurrentUser currentUser) {
        this.profileService = profileService;
        this.currentUser = currentUser;
    }

    @GetMapping
    @Operation(summary = "Profil ayarları")
    public ProfileResponse get() {
        return profileService.getOrCreate(currentUser.id());
    }

    @PutMapping
    @Operation(summary = "Profil ayarlarını güncelle")
    public ProfileResponse update(@Valid @RequestBody UpdateProfileRequest req) {
        return profileService.update(currentUser.id(), req);
    }
}
