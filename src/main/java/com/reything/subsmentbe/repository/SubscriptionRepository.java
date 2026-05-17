package com.reything.subsmentbe.repository;

import com.reything.subsmentbe.domain.Subscription;
import com.reything.subsmentbe.domain.enums.SubscriptionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, UUID>, JpaSpecificationExecutor<Subscription> {

    Optional<Subscription> findByIdAndUserId(UUID id, UUID userId);

    List<Subscription> findAllByUserId(UUID userId);

    List<Subscription> findAllByUserIdAndStatus(UUID userId, SubscriptionStatus status);

    @Query("SELECT s FROM Subscription s WHERE s.userId = :userId AND s.status = :status " +
            "AND s.nextPaymentDate BETWEEN :from AND :to ORDER BY s.nextPaymentDate ASC")
    List<Subscription> findUpcoming(@Param("userId") UUID userId,
                                     @Param("status") SubscriptionStatus status,
                                     @Param("from") LocalDate from,
                                     @Param("to") LocalDate to);

    Page<Subscription> findAllByUserId(UUID userId, Pageable pageable);
}
