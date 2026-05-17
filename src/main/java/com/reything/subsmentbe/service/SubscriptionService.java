package com.reything.subsmentbe.service;

import com.reything.subsmentbe.domain.Subscription;
import com.reything.subsmentbe.domain.enums.SubscriptionStatus;
import com.reything.subsmentbe.dto.subscription.CreateSubscriptionRequest;
import com.reything.subsmentbe.dto.subscription.StatusUpdateResponse;
import com.reything.subsmentbe.dto.subscription.SubscriptionResponse;
import com.reything.subsmentbe.dto.subscription.UpdateSubscriptionRequest;
import com.reything.subsmentbe.exception.ApiException;
import com.reything.subsmentbe.repository.SubscriptionRepository;
import com.reything.subsmentbe.util.Periods;
import jakarta.persistence.criteria.Predicate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class SubscriptionService {

    private static final Logger log = LoggerFactory.getLogger(SubscriptionService.class);

    private final SubscriptionRepository subscriptionRepository;

    public SubscriptionService(SubscriptionRepository subscriptionRepository) {
        this.subscriptionRepository = subscriptionRepository;
    }

    public Page<SubscriptionResponse> list(UUID userId, String status, String category,
                                            String search, int page, int limit) {
        log.info("[SUBSCRIPTION] Liste isteği - userId: {}, status: {}, category: {}, search: '{}', page: {}, limit: {}",
                userId, status, category, search, page, limit);
        int safeLimit = Math.min(Math.max(limit, 1), 500);
        int safePage = Math.max(page, 1);
        Pageable pageable = PageRequest.of(safePage - 1, safeLimit, Sort.by(Sort.Direction.DESC, "createdAt"));

        Specification<Subscription> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("userId"), userId));
            if (StringUtils.hasText(status) && !"all".equalsIgnoreCase(status)) {
                predicates.add(cb.equal(root.get("status"), SubscriptionStatus.valueOf(status)));
            }
            if (StringUtils.hasText(category)) {
                predicates.add(cb.equal(root.get("category"), category));
            }
            if (StringUtils.hasText(search)) {
                String like = "%" + search.toLowerCase() + "%";
                predicates.add(cb.like(cb.lower(root.get("name")), like));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return subscriptionRepository.findAll(spec, pageable).map(SubscriptionMapper::toResponse);
    }

    public SubscriptionResponse get(UUID userId, UUID id) {
        log.info("[SUBSCRIPTION] Tekil istek - userId: {}, subscriptionId: {}", userId, id);
        Subscription s = subscriptionRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> {
                    log.warn("[SUBSCRIPTION] Bulunamadı - userId: {}, subscriptionId: {}", userId, id);
                    return ApiException.notFound("Abonelik bulunamadı");
                });
        return SubscriptionMapper.toResponse(s);
    }

    @Transactional
    public SubscriptionResponse create(UUID userId, CreateSubscriptionRequest req) {
        log.info("[SUBSCRIPTION] Yeni abonelik oluşturma isteği - userId: {}, name: '{}', amount: {} {}, period: {}",
                userId, req.name(), req.amount(), req.currency(), req.period());
        if (req.firstPaymentDate() != null && req.nextPaymentDate() != null
                && req.nextPaymentDate().isBefore(req.firstPaymentDate())) {
            log.warn("[SUBSCRIPTION] Geçersiz tarih aralığı - nextPaymentDate firstPaymentDate'ten önce - userId: {}", userId);
            throw ApiException.badRequest("next_payment_date, first_payment_date'ten önce olamaz");
        }
        OffsetDateTime now = OffsetDateTime.now();
        Subscription s = Subscription.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .name(req.name())
                .category(req.category())
                .amount(req.amount())
                .currency(req.currency())
                .period(req.period())
                .customPeriodLabel(req.customPeriodLabel())
                .firstPaymentDate(req.firstPaymentDate())
                .nextPaymentDate(req.nextPaymentDate() != null
                        ? req.nextPaymentDate()
                        : Periods.calculateNext(req.firstPaymentDate(), req.period()))
                .paymentMethod(req.paymentMethod())
                .cardLastFour(req.cardLastFour())
                .status(req.status())
                .freeTrialEndDate(req.freeTrialEndDate())
                .cancelReminderDate(req.cancelReminderDate())
                .color(req.color())
                .emoji(req.emoji())
                .notes(req.notes())
                .createdAt(now)
                .updatedAt(now)
                .build();
        Subscription saved = subscriptionRepository.save(s);
        log.info("[SUBSCRIPTION] Abonelik oluşturuldu - id: {}, userId: {}, name: '{}'", saved.getId(), userId, saved.getName());
        return SubscriptionMapper.toResponse(saved);
    }

    @Transactional
    public SubscriptionResponse update(UUID userId, UUID id, UpdateSubscriptionRequest req) {
        log.info("[SUBSCRIPTION] Güncelleme isteği - userId: {}, subscriptionId: {}", userId, id);
        Subscription s = subscriptionRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> {
                    log.warn("[SUBSCRIPTION] Güncelleme başarısız - bulunamadı - userId: {}, subscriptionId: {}", userId, id);
                    return ApiException.notFound("Abonelik bulunamadı");
                });
        if (req.name() != null) s.setName(req.name());
        if (req.category() != null) s.setCategory(req.category());
        if (req.amount() != null) s.setAmount(req.amount());
        if (req.currency() != null) s.setCurrency(req.currency());
        if (req.period() != null) s.setPeriod(req.period());
        if (req.customPeriodLabel() != null) s.setCustomPeriodLabel(req.customPeriodLabel());
        if (req.firstPaymentDate() != null) s.setFirstPaymentDate(req.firstPaymentDate());
        if (req.nextPaymentDate() != null) s.setNextPaymentDate(req.nextPaymentDate());
        if (req.paymentMethod() != null) s.setPaymentMethod(req.paymentMethod());
        if (req.cardLastFour() != null) s.setCardLastFour(req.cardLastFour());
        if (req.status() != null) s.setStatus(req.status());
        if (req.freeTrialEndDate() != null) s.setFreeTrialEndDate(req.freeTrialEndDate());
        if (req.cancelReminderDate() != null) s.setCancelReminderDate(req.cancelReminderDate());
        if (req.color() != null) s.setColor(req.color());
        if (req.emoji() != null) s.setEmoji(req.emoji());
        if (req.notes() != null) s.setNotes(req.notes());
        s.setUpdatedAt(OffsetDateTime.now());
        log.info("[SUBSCRIPTION] Abonelik güncellendi - id: {}, userId: {}", id, userId);
        return SubscriptionMapper.toResponse(s);
    }

    @Transactional
    public void delete(UUID userId, UUID id) {
        log.info("[SUBSCRIPTION] Silme isteği - userId: {}, subscriptionId: {}", userId, id);
        Subscription s = subscriptionRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> {
                    log.warn("[SUBSCRIPTION] Silme başarısız - bulunamadı - userId: {}, subscriptionId: {}", userId, id);
                    return ApiException.notFound("Abonelik bulunamadı");
                });
        OffsetDateTime now = OffsetDateTime.now();
        s.setDeletedAt(now);
        s.setUpdatedAt(now);
        log.info("[SUBSCRIPTION] Abonelik silindi (soft-delete) - id: {}, userId: {}", id, userId);
    }

    @Transactional
    public StatusUpdateResponse updateStatus(UUID userId, UUID id, SubscriptionStatus status) {
        log.info("[SUBSCRIPTION] Durum güncelleme isteği - userId: {}, subscriptionId: {}, newStatus: {}", userId, id, status);
        Subscription s = subscriptionRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> {
                    log.warn("[SUBSCRIPTION] Durum güncelleme başarısız - bulunamadı - userId: {}, subscriptionId: {}", userId, id);
                    return ApiException.notFound("Abonelik bulunamadı");
                });
        s.setStatus(status);
        s.setUpdatedAt(OffsetDateTime.now());
        log.info("[SUBSCRIPTION] Durum güncellendi - id: {}, userId: {}, status: {}", id, userId, status);
        return new StatusUpdateResponse(s.getId(), s.getStatus(), s.getUpdatedAt());
    }

    public Subscription requireOwned(UUID userId, UUID subscriptionId) {
        return subscriptionRepository.findByIdAndUserId(subscriptionId, userId)
                .orElseThrow(() -> ApiException.notFound("Abonelik bulunamadı"));
    }
}
