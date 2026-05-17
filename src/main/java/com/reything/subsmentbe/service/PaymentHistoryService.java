package com.reything.subsmentbe.service;

import com.reything.subsmentbe.domain.PaymentHistory;
import com.reything.subsmentbe.domain.Subscription;
import com.reything.subsmentbe.dto.payment.CreatePaymentRequest;
import com.reything.subsmentbe.dto.payment.PaymentHistoryResponse;
import com.reything.subsmentbe.repository.PaymentHistoryRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class PaymentHistoryService {

    private final PaymentHistoryRepository repository;
    private final SubscriptionService subscriptionService;

    public PaymentHistoryService(PaymentHistoryRepository repository, SubscriptionService subscriptionService) {
        this.repository = repository;
        this.subscriptionService = subscriptionService;
    }

    public List<PaymentHistoryResponse> list(UUID userId, UUID subscriptionId, int limit) {
        subscriptionService.requireOwned(userId, subscriptionId);
        int safeLimit = Math.min(Math.max(limit, 1), 500);
        return repository.findAllBySubscriptionIdOrderByPaymentDateDesc(subscriptionId, PageRequest.of(0, safeLimit))
                .stream().map(this::toResponse).toList();
    }

    @Transactional
    public PaymentHistoryResponse create(UUID userId, UUID subscriptionId, CreatePaymentRequest req) {
        Subscription s = subscriptionService.requireOwned(userId, subscriptionId);
        PaymentHistory ph = PaymentHistory.builder()
                .id(UUID.randomUUID())
                .subscriptionId(s.getId())
                .paymentDate(req.paymentDate())
                .amount(s.getAmount())
                .currency(s.getCurrency())
                .status(req.status())
                .createdAt(OffsetDateTime.now())
                .build();
        return toResponse(repository.save(ph));
    }

    private PaymentHistoryResponse toResponse(PaymentHistory ph) {
        return new PaymentHistoryResponse(
                ph.getId(), ph.getSubscriptionId(), ph.getPaymentDate(),
                ph.getAmount(), ph.getCurrency(), ph.getStatus(), ph.getCreatedAt()
        );
    }
}
