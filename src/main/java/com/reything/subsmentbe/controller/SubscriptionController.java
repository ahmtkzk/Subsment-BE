package com.reything.subsmentbe.controller;

import com.reything.subsmentbe.dto.common.MessageResponse;
import com.reything.subsmentbe.dto.common.PaginationInfo;
import com.reything.subsmentbe.dto.subscription.CreateSubscriptionRequest;
import com.reything.subsmentbe.dto.subscription.StatusUpdateResponse;
import com.reything.subsmentbe.dto.subscription.SubscriptionResponse;
import com.reything.subsmentbe.dto.subscription.UpdateStatusRequest;
import com.reything.subsmentbe.dto.subscription.UpdateSubscriptionRequest;
import com.reything.subsmentbe.security.CurrentUser;
import com.reything.subsmentbe.service.SubscriptionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/subscriptions")
@Tag(name = "Subscription")
@SecurityRequirement(name = "bearerAuth")
public class SubscriptionController {

    private final SubscriptionService subscriptionService;
    private final CurrentUser currentUser;

    public SubscriptionController(SubscriptionService subscriptionService, CurrentUser currentUser) {
        this.subscriptionService = subscriptionService;
        this.currentUser = currentUser;
    }

    public record ListResponse(boolean success, List<SubscriptionResponse> subscriptions, PaginationInfo pagination) {
    }

    @GetMapping
    @Operation(summary = "Abonelik listesi")
    public ListResponse list(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "50") int limit) {
        Page<SubscriptionResponse> result = subscriptionService.list(currentUser.id(), status, category, search, page, limit);
        return new ListResponse(true, result.getContent(),
                new PaginationInfo(result.getNumber() + 1, result.getSize(), result.getTotalElements(), result.getTotalPages()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Abonelik detayı")
    public SubscriptionResponse get(@PathVariable UUID id) {
        return subscriptionService.get(currentUser.id(), id);
    }

    @PostMapping
    @Operation(summary = "Abonelik oluştur")
    public ResponseEntity<SubscriptionResponse> create(@Valid @RequestBody CreateSubscriptionRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(subscriptionService.create(currentUser.id(), req));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Abonelik güncelle")
    public SubscriptionResponse update(@PathVariable UUID id, @Valid @RequestBody UpdateSubscriptionRequest req) {
        return subscriptionService.update(currentUser.id(), id, req);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Abonelik sil")
    public MessageResponse delete(@PathVariable UUID id) {
        subscriptionService.delete(currentUser.id(), id);
        return MessageResponse.of("Abonelik silindi");
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Abonelik durumu güncelle")
    public StatusUpdateResponse updateStatus(@PathVariable UUID id, @Valid @RequestBody UpdateStatusRequest req) {
        return subscriptionService.updateStatus(currentUser.id(), id, req.status());
    }
}
