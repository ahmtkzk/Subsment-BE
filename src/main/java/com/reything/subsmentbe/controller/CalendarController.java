package com.reything.subsmentbe.controller;

import com.reything.subsmentbe.dto.calendar.CalendarDay;
import com.reything.subsmentbe.dto.calendar.CalendarSubscription;
import com.reything.subsmentbe.domain.enums.Currency;
import com.reything.subsmentbe.security.CurrentUser;
import com.reything.subsmentbe.service.CalendarService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/calendar")
@Tag(name = "Calendar")
@SecurityRequirement(name = "bearerAuth")
public class CalendarController {

    private final CalendarService calendarService;
    private final CurrentUser currentUser;

    public CalendarController(CalendarService calendarService, CurrentUser currentUser) {
        this.calendarService = calendarService;
        this.currentUser = currentUser;
    }

    public record PaymentsResponse(boolean success, List<CalendarDay> payments) {
    }

    public record DayResponse(boolean success, LocalDate date, List<CalendarSubscription> subscriptions, BigDecimal totalAmount, Currency currency) {
    }

    @GetMapping("/payments")
    @Operation(summary = "Aya göre ödemeler")
    public PaymentsResponse payments(@RequestParam int year, @RequestParam int month) {
        return new PaymentsResponse(true, calendarService.paymentsForMonth(currentUser.id(), year, month));
    }

    @GetMapping("/day")
    @Operation(summary = "Belirli gün detayı")
    public DayResponse day(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        CalendarService.DayDetails details = calendarService.day(currentUser.id(), date);
        return new DayResponse(true, details.date(), details.subscriptions(), details.totalAmount(), details.currency());
    }
}
