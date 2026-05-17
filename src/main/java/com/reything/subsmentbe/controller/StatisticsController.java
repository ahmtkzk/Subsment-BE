package com.reything.subsmentbe.controller;

import com.reything.subsmentbe.dto.statistics.CategoryStatistic;
import com.reything.subsmentbe.dto.statistics.CurrencyStatistic;
import com.reything.subsmentbe.dto.statistics.DashboardStatistics;
import com.reything.subsmentbe.dto.statistics.MonthlyBreakdown;
import com.reything.subsmentbe.dto.statistics.TopSubscription;
import com.reything.subsmentbe.security.CurrentUser;
import com.reything.subsmentbe.service.StatisticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/statistics")
@Tag(name = "Statistics")
@SecurityRequirement(name = "bearerAuth")
public class StatisticsController {

    private final StatisticsService statisticsService;
    private final CurrentUser currentUser;

    public StatisticsController(StatisticsService statisticsService, CurrentUser currentUser) {
        this.statisticsService = statisticsService;
        this.currentUser = currentUser;
    }

    public record DashboardResponse(boolean success, DashboardStatistics statistics) {
    }

    public record CategoriesResponse(boolean success, List<CategoryStatistic> categories) {
    }

    public record CurrenciesResponse(boolean success, List<CurrencyStatistic> currencies) {
    }

    public record MonthsResponse(boolean success, List<MonthlyBreakdown> months) {
    }

    public record TopResponse(boolean success, List<TopSubscription> subscriptions) {
    }

    @GetMapping("/dashboard")
    @Operation(summary = "Dashboard istatistikleri")
    public DashboardResponse dashboard() {
        return new DashboardResponse(true, statisticsService.dashboard(currentUser.id()));
    }

    @GetMapping("/categories")
    @Operation(summary = "Kategori istatistikleri")
    public CategoriesResponse categories() {
        return new CategoriesResponse(true, statisticsService.categories(currentUser.id()));
    }

    @GetMapping("/currencies")
    @Operation(summary = "Para birimi istatistikleri")
    public CurrenciesResponse currencies() {
        return new CurrenciesResponse(true, statisticsService.currencies(currentUser.id()));
    }

    @GetMapping("/monthly")
    @Operation(summary = "Aylık dağılım")
    public MonthsResponse monthly(@RequestParam(defaultValue = "12") int months) {
        return new MonthsResponse(true, statisticsService.monthlyBreakdown(currentUser.id(), months));
    }

    @GetMapping("/top-subscriptions")
    @Operation(summary = "En çok harcama yapılan abonelikler")
    public TopResponse top(@RequestParam(defaultValue = "10") int limit) {
        return new TopResponse(true, statisticsService.top(currentUser.id(), limit));
    }
}
