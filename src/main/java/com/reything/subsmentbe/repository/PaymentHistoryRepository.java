package com.reything.subsmentbe.repository;

import com.reything.subsmentbe.domain.PaymentHistory;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PaymentHistoryRepository extends JpaRepository<PaymentHistory, UUID> {
    List<PaymentHistory> findAllBySubscriptionIdOrderByPaymentDateDesc(UUID subscriptionId, Pageable pageable);
}
