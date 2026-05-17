package com.reything.subsmentbe.controller;

import com.reything.subsmentbe.dto.payment.CreatePaymentRequest;
import com.reything.subsmentbe.dto.payment.PaymentHistoryResponse;
import com.reything.subsmentbe.security.CurrentUser;
import com.reything.subsmentbe.service.PaymentHistoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/subscriptions/{id}/payment-history")
@Tag(name = "PaymentHistory")
@SecurityRequirement(name = "bearerAuth")
public class PaymentHistoryController {

    private final PaymentHistoryService paymentHistoryService;
    private final CurrentUser currentUser;

    public PaymentHistoryController(PaymentHistoryService paymentHistoryService, CurrentUser currentUser) {
        this.paymentHistoryService = paymentHistoryService;
        this.currentUser = currentUser;
    }

    public record PaymentListResponse(boolean success, List<PaymentHistoryResponse> payments) {
    }

    public record PaymentCreatedResponse(boolean success, PaymentHistoryResponse payment) {
    }

    @GetMapping
    @Operation(summary = "Ödeme geçmişi")
    public PaymentListResponse list(@PathVariable UUID id, @RequestParam(defaultValue = "12") int limit) {
        return new PaymentListResponse(true, paymentHistoryService.list(currentUser.id(), id, limit));
    }

    @PostMapping
    @Operation(summary = "Ödeme kaydı oluştur")
    public ResponseEntity<PaymentCreatedResponse> create(@PathVariable UUID id, @Valid @RequestBody CreatePaymentRequest req) {
        PaymentHistoryResponse payment = paymentHistoryService.create(currentUser.id(), id, req);
        return ResponseEntity.status(HttpStatus.CREATED).body(new PaymentCreatedResponse(true, payment));
    }
}
