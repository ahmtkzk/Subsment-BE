package com.reything.subsmentbe.service;

import com.reything.subsmentbe.domain.Profile;
import com.reything.subsmentbe.domain.Subscription;
import com.reything.subsmentbe.domain.enums.CategoryType;
import com.reything.subsmentbe.domain.enums.Currency;
import com.reything.subsmentbe.domain.enums.SubscriptionStatus;
import com.reything.subsmentbe.dto.statistics.CategoryStatistic;
import com.reything.subsmentbe.dto.statistics.CurrencyStatistic;
import com.reything.subsmentbe.dto.statistics.DashboardStatistics;
import com.reything.subsmentbe.dto.statistics.MonthlyBreakdown;
import com.reything.subsmentbe.dto.statistics.TopSubscription;
import com.reything.subsmentbe.dto.statistics.UpcomingPayment;
import com.reything.subsmentbe.repository.SubscriptionRepository;
import com.reything.subsmentbe.util.Periods;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class StatisticsService {

    private final SubscriptionRepository subscriptionRepository;
    private final ProfileService profileService;
    private final CurrencyConversionService currencyConversionService;

    public StatisticsService(SubscriptionRepository subscriptionRepository,
                              ProfileService profileService,
                              CurrencyConversionService currencyConversionService) {
        this.subscriptionRepository = subscriptionRepository;
        this.profileService = profileService;
        this.currencyConversionService = currencyConversionService;
    }

    public DashboardStatistics dashboard(UUID userId) {
        Profile profile = profileService.loadOrDefault(userId);
        Currency target = profile.getPrimaryCurrency();
        boolean convert = Boolean.TRUE.equals(profile.getConvertForeignCurrency());

        List<Subscription> all = subscriptionRepository.findAllByUserId(userId);

        BigDecimal monthly = BigDecimal.ZERO;
        long active = 0, inactive = 0, cancelled = 0;
        for (Subscription s : all) {
            switch (s.getStatus()) {
                case active -> active++;
                case inactive -> inactive++;
                case cancelled -> cancelled++;
            }
            if (s.getStatus() == SubscriptionStatus.active) {
                BigDecimal m = Periods.monthlyEquivalent(s.getAmount(), s.getPeriod());
                BigDecimal converted = convert ? currencyConversionService.convert(m, s.getCurrency(), target) : m;
                monthly = monthly.add(converted);
            }
        }
        monthly = monthly.setScale(2, RoundingMode.HALF_UP);
        BigDecimal yearly = monthly.multiply(new BigDecimal("12")).setScale(2, RoundingMode.HALF_UP);

        LocalDate today = LocalDate.now();
        LocalDate end = today.plusDays(10);
        List<UpcomingPayment> upcoming = subscriptionRepository
                .findUpcoming(userId, SubscriptionStatus.active, today, end)
                .stream()
                .map(s -> new UpcomingPayment(
                        s.getId(),
                        s.getName(),
                        s.getAmount(),
                        s.getCurrency(),
                        s.getNextPaymentDate(),
                        ChronoUnit.DAYS.between(today, s.getNextPaymentDate()),
                        s.getStatus()))
                .toList();

        return new DashboardStatistics(monthly, yearly, active, inactive, cancelled, upcoming);
    }

    public List<CategoryStatistic> categories(UUID userId) {
        Profile profile = profileService.loadOrDefault(userId);
        Currency target = profile.getPrimaryCurrency();
        boolean convert = Boolean.TRUE.equals(profile.getConvertForeignCurrency());

        List<Subscription> active = subscriptionRepository.findAllByUserIdAndStatus(userId, SubscriptionStatus.active);

        Map<String, BigDecimal> totals = new HashMap<>();
        Map<String, Long> counts = new HashMap<>();
        for (Subscription s : active) {
            BigDecimal monthly = Periods.monthlyEquivalent(s.getAmount(), s.getPeriod());
            BigDecimal v = convert ? currencyConversionService.convert(monthly, s.getCurrency(), target) : monthly;
            totals.merge(s.getCategory(), v, BigDecimal::add);
            counts.merge(s.getCategory(), 1L, Long::sum);
        }

        List<CategoryStatistic> out = new ArrayList<>();
        for (Map.Entry<String, BigDecimal> e : totals.entrySet()) {
            String name = e.getKey();
            CategoryType type = findCategory(name);
            out.add(new CategoryStatistic(
                    name,
                    type != null ? type.getEmoji() : "📦",
                    type != null ? type.getColor() : "#95A5A6",
                    counts.getOrDefault(name, 0L),
                    e.getValue().setScale(2, RoundingMode.HALF_UP),
                    target
            ));
        }
        out.sort(Comparator.comparing(CategoryStatistic::totalAmount).reversed());
        return out;
    }

    public List<CurrencyStatistic> currencies(UUID userId) {
        List<Subscription> active = subscriptionRepository.findAllByUserIdAndStatus(userId, SubscriptionStatus.active);
        Map<Currency, BigDecimal> totals = new HashMap<>();
        Map<Currency, Long> counts = new HashMap<>();
        BigDecimal grand = BigDecimal.ZERO;
        for (Subscription s : active) {
            BigDecimal monthly = Periods.monthlyEquivalent(s.getAmount(), s.getPeriod());
            totals.merge(s.getCurrency(), monthly, BigDecimal::add);
            counts.merge(s.getCurrency(), 1L, Long::sum);
            grand = grand.add(currencyConversionService.convert(monthly, s.getCurrency(), Currency.TRY));
        }
        List<CurrencyStatistic> out = new ArrayList<>();
        for (Map.Entry<Currency, BigDecimal> e : totals.entrySet()) {
            BigDecimal inTry = currencyConversionService.convert(e.getValue(), e.getKey(), Currency.TRY);
            BigDecimal pct = grand.signum() == 0
                    ? BigDecimal.ZERO
                    : inTry.multiply(new BigDecimal("100")).divide(grand, 2, RoundingMode.HALF_UP);
            out.add(new CurrencyStatistic(
                    e.getKey(), e.getKey().getSymbol(),
                    e.getValue().setScale(2, RoundingMode.HALF_UP),
                    counts.getOrDefault(e.getKey(), 0L), pct));
        }
        out.sort(Comparator.comparing(CurrencyStatistic::percentage).reversed());
        return out;
    }

    public List<MonthlyBreakdown> monthlyBreakdown(UUID userId, int months) {
        Profile profile = profileService.loadOrDefault(userId);
        Currency target = profile.getPrimaryCurrency();
        boolean convert = Boolean.TRUE.equals(profile.getConvertForeignCurrency());

        int safeMonths = Math.min(Math.max(months, 1), 60);
        List<Subscription> all = subscriptionRepository.findAllByUserId(userId);

        List<MonthlyBreakdown> out = new ArrayList<>();
        YearMonth current = YearMonth.now();
        for (int i = safeMonths - 1; i >= 0; i--) {
            YearMonth ym = current.minusMonths(i);
            BigDecimal total = BigDecimal.ZERO;
            long count = 0;
            for (Subscription s : all) {
                if (s.getStatus() != SubscriptionStatus.active) continue;
                YearMonth start = YearMonth.from(s.getFirstPaymentDate());
                if (ym.isBefore(start)) continue;
                BigDecimal monthly = Periods.monthlyEquivalent(s.getAmount(), s.getPeriod());
                BigDecimal v = convert ? currencyConversionService.convert(monthly, s.getCurrency(), target) : monthly;
                total = total.add(v);
                count++;
            }
            out.add(new MonthlyBreakdown(ym.toString(), total.setScale(2, RoundingMode.HALF_UP), target, count));
        }
        return out;
    }

    public List<TopSubscription> top(UUID userId, int limit) {
        int safeLimit = Math.min(Math.max(limit, 1), 100);
        Profile profile = profileService.loadOrDefault(userId);
        Currency target = profile.getPrimaryCurrency();
        boolean convert = Boolean.TRUE.equals(profile.getConvertForeignCurrency());

        return subscriptionRepository.findAllByUserIdAndStatus(userId, SubscriptionStatus.active).stream()
                .sorted(Comparator.comparing((Subscription s) -> {
                    BigDecimal monthly = Periods.monthlyEquivalent(s.getAmount(), s.getPeriod());
                    return convert ? currencyConversionService.convert(monthly, s.getCurrency(), target) : monthly;
                }).reversed())
                .limit(safeLimit)
                .map(s -> new TopSubscription(s.getId(), s.getName(), s.getAmount(), s.getCurrency(), s.getCategory(), s.getEmoji()))
                .toList();
    }

    private CategoryType findCategory(String displayName) {
        for (CategoryType t : CategoryType.values()) {
            if (t.getDisplayName().equals(displayName)) return t;
        }
        return null;
    }
}
