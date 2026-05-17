package com.reything.subsmentbe.service;

import com.reything.subsmentbe.domain.User;
import com.reything.subsmentbe.dto.user.UpdateUserRequest;
import com.reything.subsmentbe.dto.user.UserResponse;
import com.reything.subsmentbe.exception.ApiException;
import com.reything.subsmentbe.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.OffsetDateTime;
import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public UserResponse getMe(UUID userId) {
        User u = userRepository.findById(userId).orElseThrow(() -> ApiException.notFound("Kullanıcı bulunamadı"));
        return new UserResponse(u.getId(), u.getName(), u.getEmail(), u.getCreatedAt());
    }

    @Transactional
    public UserResponse updateMe(UUID userId, UpdateUserRequest req) {
        User u = userRepository.findById(userId).orElseThrow(() -> ApiException.notFound("Kullanıcı bulunamadı"));
        if (StringUtils.hasText(req.name())) u.setName(req.name());
        if (StringUtils.hasText(req.email()) && !req.email().equalsIgnoreCase(u.getEmail())) {
            if (userRepository.existsByEmail(req.email())) {
                throw ApiException.conflict("Bu e-posta zaten kayıtlı");
            }
            u.setEmail(req.email());
        }
        if (StringUtils.hasText(req.password())) {
            if (!StringUtils.hasText(req.oldPassword())
                    || !passwordEncoder.matches(req.oldPassword(), u.getPassword())) {
                throw ApiException.badRequest("Eski şifre hatalı");
            }
            u.setPassword(passwordEncoder.encode(req.password()));
        }
        u.setUpdatedAt(OffsetDateTime.now());
        return new UserResponse(u.getId(), u.getName(), u.getEmail(), u.getCreatedAt());
    }

    @Transactional
    public void deleteMe(UUID userId) {
        User u = userRepository.findById(userId).orElseThrow(() -> ApiException.notFound("Kullanıcı bulunamadı"));
        u.setDeletedAt(OffsetDateTime.now());
        u.setUpdatedAt(OffsetDateTime.now());
    }
}
