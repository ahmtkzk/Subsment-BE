package com.reything.subsmentbe.service;

import com.reything.subsmentbe.domain.User;
import com.reything.subsmentbe.dto.user.UpdateUserRequest;
import com.reything.subsmentbe.dto.user.UserResponse;
import com.reything.subsmentbe.exception.ApiException;
import com.reything.subsmentbe.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.OffsetDateTime;
import java.util.UUID;

@Service
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public UserResponse getMe(UUID userId) {
        log.info("[USER] Profil bilgisi istendi - userId: {}", userId);
        User u = userRepository.findById(userId).orElseThrow(() -> {
            log.warn("[USER] Kullanıcı bulunamadı - userId: {}", userId);
            return ApiException.notFound("Kullanıcı bulunamadı");
        });
        return new UserResponse(u.getId(), u.getName(), u.getEmail(), u.getCreatedAt());
    }

    @Transactional
    public UserResponse updateMe(UUID userId, UpdateUserRequest req) {
        log.info("[USER] Güncelleme isteği - userId: {}", userId);
        User u = userRepository.findById(userId).orElseThrow(() -> {
            log.warn("[USER] Kullanıcı bulunamadı - userId: {}", userId);
            return ApiException.notFound("Kullanıcı bulunamadı");
        });
        if (StringUtils.hasText(req.name())) u.setName(req.name());
        if (StringUtils.hasText(req.email()) && !req.email().equalsIgnoreCase(u.getEmail())) {
            if (userRepository.existsByEmail(req.email())) {
                log.warn("[USER] E-posta zaten kayıtlı - email: {}, userId: {}", req.email(), userId);
                throw ApiException.conflict("Bu e-posta zaten kayıtlı");
            }
            u.setEmail(req.email());
        }
        if (StringUtils.hasText(req.password())) {
            if (!StringUtils.hasText(req.oldPassword())
                    || !passwordEncoder.matches(req.oldPassword(), u.getPassword())) {
                log.warn("[USER] Eski şifre hatalı - userId: {}", userId);
                throw ApiException.badRequest("Eski şifre hatalı");
            }
            u.setPassword(passwordEncoder.encode(req.password()));
            log.info("[USER] Şifre güncellendi - userId: {}", userId);
        }
        u.setUpdatedAt(OffsetDateTime.now());
        log.info("[USER] Kullanıcı güncellendi - userId: {}", userId);
        return new UserResponse(u.getId(), u.getName(), u.getEmail(), u.getCreatedAt());
    }

    @Transactional
    public void deleteMe(UUID userId) {
        log.info("[USER] Hesap silme isteği - userId: {}", userId);
        User u = userRepository.findById(userId).orElseThrow(() -> {
            log.warn("[USER] Kullanıcı bulunamadı - userId: {}", userId);
            return ApiException.notFound("Kullanıcı bulunamadı");
        });
        u.setDeletedAt(OffsetDateTime.now());
        u.setUpdatedAt(OffsetDateTime.now());
        log.info("[USER] Hesap silindi (soft-delete) - userId: {}", userId);
    }
}
