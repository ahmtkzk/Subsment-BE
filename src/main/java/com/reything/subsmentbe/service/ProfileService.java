package com.reything.subsmentbe.service;

import com.reything.subsmentbe.domain.Profile;
import com.reything.subsmentbe.domain.enums.Currency;
import com.reything.subsmentbe.dto.profile.ProfileResponse;
import com.reything.subsmentbe.dto.profile.UpdateProfileRequest;
import com.reything.subsmentbe.repository.ProfileRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.UUID;

@Service
public class ProfileService {

    private final ProfileRepository profileRepository;

    public ProfileService(ProfileRepository profileRepository) {
        this.profileRepository = profileRepository;
    }

    @Transactional
    public ProfileResponse getOrCreate(UUID userId) {
        Profile p = profileRepository.findByUserId(userId).orElseGet(() -> createDefault(userId));
        return toResponse(p);
    }

    @Transactional
    public ProfileResponse update(UUID userId, UpdateProfileRequest req) {
        Profile p = profileRepository.findByUserId(userId).orElseGet(() -> createDefault(userId));
        if (req.primaryCurrency() != null) p.setPrimaryCurrency(req.primaryCurrency());
        if (req.convertForeignCurrency() != null) p.setConvertForeignCurrency(req.convertForeignCurrency());
        if (req.darkMode() != null) p.setDarkMode(req.darkMode());
        if (req.notificationsEnabled() != null) p.setNotificationsEnabled(req.notificationsEnabled());
        if (req.cancelReminderDays() != null) p.setCancelReminderDays(req.cancelReminderDays());
        p.setUpdatedAt(OffsetDateTime.now());
        return toResponse(p);
    }

    public Profile loadOrDefault(UUID userId) {
        return profileRepository.findByUserId(userId).orElseGet(() -> createDefault(userId));
    }

    private Profile createDefault(UUID userId) {
        OffsetDateTime now = OffsetDateTime.now();
        Profile p = Profile.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .primaryCurrency(Currency.TRY)
                .convertForeignCurrency(true)
                .darkMode(false)
                .notificationsEnabled(true)
                .cancelReminderDays(3)
                .createdAt(now)
                .updatedAt(now)
                .build();
        return profileRepository.save(p);
    }

    private ProfileResponse toResponse(Profile p) {
        return new ProfileResponse(
                p.getId(), p.getUserId(), p.getPrimaryCurrency(),
                p.getConvertForeignCurrency(), p.getDarkMode(),
                p.getNotificationsEnabled(), p.getCancelReminderDays(),
                p.getCreatedAt(), p.getUpdatedAt()
        );
    }
}
