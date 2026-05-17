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

    private final SubscriptionRepository subscriptionRepository;

    public SubscriptionService(SubscriptionRepository subscriptionRepository) {
        this.subscriptionRepository = subscriptionRepository;
    }

    public Page<SubscriptionResponse> list(UUID userId, String status, String category,
                                            String search, int page, int limit) {
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
        Subscription s = subscriptionRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> ApiException.notFound("Abonelik bulunamadı"));
        return SubscriptionMapper.toResponse(s);
    }

    @Transactional
    public SubscriptionResponse create(UUID userId, CreateSubscriptionRequest req) {
        if (req.firstPaymentDate() != null && req.nextPaymentDate() != null
                && req.nextPaymentDate().isBefore(req.firstPaymentDate())) {
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
        return SubscriptionMapper.toResponse(saved);
    }

    @Transactional
    public SubscriptionResponse update(UUID userId, UUID id, UpdateSubscriptionRequest req) {
        Subscription s = subscriptionRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> ApiException.notFound("Abonelik bulunamadı"));
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
        return SubscriptionMapper.toResponse(s);
    }

    @Transactional
    public void delete(UUID userId, UUID id) {
        Subscription s = subscriptionRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> ApiException.notFound("Abonelik bulunamadı"));
        OffsetDateTime now = OffsetDateTime.now();
        s.setDeletedAt(now);
        s.setUpdatedAt(now);
    }

    @Transactional
    public StatusUpdateResponse updateStatus(UUID userId, UUID id, SubscriptionStatus status) {
        Subscription s = subscriptionRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> ApiException.notFound("Abonelik bulunamadı"));
        s.setStatus(status);
        s.setUpdatedAt(OffsetDateTime.now());
        return new StatusUpdateResponse(s.getId(), s.getStatus(), s.getUpdatedAt());
    }

    public Subscription requireOwned(UUID userId, UUID subscriptionId) {
        return subscriptionRepository.findByIdAndUserId(subscriptionId, userId)
                .orElseThrow(() -> ApiException.notFound("Abonelik bulunamadı"));
    }
}
