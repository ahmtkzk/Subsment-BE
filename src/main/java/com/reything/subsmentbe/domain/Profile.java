package com.reything.subsmentbe.domain;

import com.reything.subsmentbe.domain.enums.Currency;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "profiles", indexes = {
        @Index(name = "idx_profile_user_id", columnList = "user_id", unique = true)
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Profile {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "user_id", nullable = false, unique = true)
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "primary_currency", nullable = false, length = 8)
    private Currency primaryCurrency;

    @Column(name = "convert_foreign_currency", nullable = false)
    private Boolean convertForeignCurrency;

    @Column(name = "dark_mode", nullable = false)
    private Boolean darkMode;

    @Column(name = "notifications_enabled", nullable = false)
    private Boolean notificationsEnabled;

    @Column(name = "cancel_reminder_days", nullable = false)
    private Integer cancelReminderDays;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;
}
