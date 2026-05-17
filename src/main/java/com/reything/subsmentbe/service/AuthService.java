package com.reything.subsmentbe.service;

import com.reything.subsmentbe.domain.Profile;
import com.reything.subsmentbe.domain.User;
import com.reything.subsmentbe.domain.enums.Currency;
import com.reything.subsmentbe.dto.auth.AuthResponse;
import com.reything.subsmentbe.dto.auth.LoginRequest;
import com.reything.subsmentbe.dto.auth.RegisterRequest;
import com.reything.subsmentbe.dto.user.UserSummary;
import com.reything.subsmentbe.exception.ApiException;
import com.reything.subsmentbe.repository.ProfileRepository;
import com.reything.subsmentbe.repository.UserRepository;
import com.reything.subsmentbe.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.UUID;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository, ProfileRepository profileRepository,
                       PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.profileRepository = profileRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public AuthResponse register(RegisterRequest req) {
        if (userRepository.existsByEmail(req.email())) {
            throw ApiException.conflict("Bu e-posta zaten kayıtlı");
        }
        OffsetDateTime now = OffsetDateTime.now();
        User user = User.builder()
                .id(UUID.randomUUID())
                .email(req.email())
                .name(req.name())
                .password(passwordEncoder.encode(req.password()))
                .createdAt(now)
                .updatedAt(now)
                .build();
        userRepository.save(user);

        Profile profile = Profile.builder()
                .id(UUID.randomUUID())
                .userId(user.getId())
                .primaryCurrency(Currency.TRY)
                .convertForeignCurrency(true)
                .darkMode(false)
                .notificationsEnabled(true)
                .cancelReminderDays(3)
                .createdAt(now)
                .updatedAt(now)
                .build();
        profileRepository.save(profile);

        String token = jwtService.generateAccessToken(user.getId(), user.getEmail());
        return new AuthResponse(true, new UserSummary(user.getId(), user.getName(), user.getEmail()), token, "Kayıt başarılı");
    }

    public AuthResponse login(LoginRequest req) {
        User user = userRepository.findByEmail(req.email())
                .orElseThrow(() -> ApiException.notFound("Kullanıcı bulunamadı"));
        if (!passwordEncoder.matches(req.password(), user.getPassword())) {
            throw ApiException.unauthorized("Geçersiz kullanıcı adı veya şifre");
        }
        String token = jwtService.generateAccessToken(user.getId(), user.getEmail());
        return new AuthResponse(true, new UserSummary(user.getId(), user.getName(), user.getEmail()), token, "Giriş başarılı");
    }

    public String refresh(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> ApiException.unauthorized("Geçersiz token"));
        return jwtService.generateAccessToken(user.getId(), user.getEmail());
    }
}
