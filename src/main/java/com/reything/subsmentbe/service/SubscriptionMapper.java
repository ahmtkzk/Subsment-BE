package com.reything.subsmentbe.service;

import com.reything.subsmentbe.domain.Subscription;
import com.reything.subsmentbe.dto.subscription.SubscriptionResponse;

public final class SubscriptionMapper {

    private SubscriptionMapper() {
    }

    public static SubscriptionResponse toResponse(Subscription s) {
        return new SubscriptionResponse(
                s.getId(),
                s.getName(),
                s.getCategory(),
                s.getAmount(),
                s.getCurrency(),
                s.getPeriod(),
                s.getCustomPeriodLabel(),
                s.getFirstPaymentDate(),
                s.getNextPaymentDate(),
                s.getPaymentMethod(),
                s.getCardLastFour(),
                s.getStatus(),
                s.getFreeTrialEndDate(),
                s.getCancelReminderDate(),
                s.getColor(),
                s.getEmoji(),
                s.getNotes(),
                s.getCreatedAt(),
                s.getUpdatedAt()
        );
    }
}
