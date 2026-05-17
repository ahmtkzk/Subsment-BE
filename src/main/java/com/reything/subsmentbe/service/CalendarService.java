package com.reything.subsmentbe.service;

import com.reything.subsmentbe.domain.Profile;
import com.reything.subsmentbe.domain.Subscription;
import com.reything.subsmentbe.domain.enums.Currency;
import com.reything.subsmentbe.domain.enums.Period;
import com.reything.subsmentbe.domain.enums.SubscriptionStatus;
import com.reything.subsmentbe.dto.calendar.CalendarDay;
import com.reything.subsmentbe.dto.calendar.CalendarSubscription;
import com.reything.subsmentbe.repository.SubscriptionRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

@Service
public class CalendarService {

    private final SubscriptionRepository subscriptionRepository;
    private final ProfileService profileService;
    private final CurrencyConversionService currencyConversionService;

    public CalendarService(SubscriptionRepository subscriptionRepository,
                            ProfileService profileService,
                            CurrencyConversionService currencyConversionService) {
        this.subscriptionRepository = subscriptionRepository;
        this.profileService = profileService;
        this.currencyConversionService = currencyConversionService;
    }

    public List<CalendarDay> paymentsForMonth(UUID userId, int year, int month) {
        YearMonth ym = YearMonth.of(year, month);
        LocalDate start = ym.atDay(1);
        LocalDate end = ym.atEndOfMonth();

        List<Subscription> all = subscriptionRepository.findAllByUserId(userId);
        Map<Integer, List<CalendarSubscription>> grouped = new TreeMap<>();

        for (Subscription s : all) {
            if (s.getStatus() == SubscriptionStatus.cancelled) continue;
            for (LocalDate d : occurrencesInRange(s, start, end)) {
                grouped.computeIfAbsent(d.getDayOfMonth(), k -> new ArrayList<>())
                        .add(new CalendarSubscription(
                                s.getId(), s.getName(), s.getAmount(), s.getCurrency(),
                                s.getEmoji(), s.getColor(), s.getStatus(), s.getCategory()));
            }
        }

        List<CalendarDay> out = new ArrayList<>();
        for (Map.Entry<Integer, List<CalendarSubscription>> e : grouped.entrySet()) {
            out.add(new CalendarDay(e.getKey(), e.getValue()));
        }
        return out;
    }

    public DayDetails day(UUID userId, LocalDate date) {
        List<Subscription> all = subscriptionRepository.findAllByUserId(userId);
        Profile profile = profileService.loadOrDefault(userId);
        Currency target = profile.getPrimaryCurrency();
        boolean convert = Boolean.TRUE.equals(profile.getConvertForeignCurrency());

        List<CalendarSubscription> hits = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;
        for (Subscription s : all) {
            if (s.getStatus() == SubscriptionStatus.cancelled) continue;
            if (occurrencesInRange(s, date, date).contains(date)) {
                hits.add(new CalendarSubscription(
                        s.getId(), s.getName(), s.getAmount(), s.getCurrency(),
                        s.getEmoji(), s.getColor(), s.getStatus(), s.getCategory()));
                BigDecimal v = convert ? currencyConversionService.convert(s.getAmount(), s.getCurrency(), target) : s.getAmount();
                total = total.add(v);
            }
        }
        return new DayDetails(date, hits, total.setScale(2, RoundingMode.HALF_UP), target);
    }

    private List<LocalDate> occurrencesInRange(Subscription s, LocalDate from, LocalDate to) {
        List<LocalDate> out = new ArrayList<>();
        LocalDate first = s.getFirstPaymentDate();
        if (first == null || first.isAfter(to)) return out;
        Period period = s.getPeriod();
        if (period == Period.custom) {
            if (s.getNextPaymentDate() != null
                    && !s.getNextPaymentDate().isBefore(from)
                    && !s.getNextPaymentDate().isAfter(to)) {
                out.add(s.getNextPaymentDate());
            }
            return out;
        }
        LocalDate d = first;
        int guard = 0;
        while (d.isBefore(from) && guard++ < 10000) {
            d = advance(d, period);
        }
        while (!d.isAfter(to) && guard++ < 10000) {
            out.add(d);
            d = advance(d, period);
        }
        return out;
    }

    private LocalDate advance(LocalDate d, Period period) {
        return switch (period) {
            case monthly -> d.plusMonths(1);
            case yearly -> d.plusYears(1);
            case weekly -> d.plusWeeks(1);
            case custom -> d.plusYears(100);
        };
    }

    public record DayDetails(LocalDate date, List<CalendarSubscription> subscriptions, BigDecimal totalAmount, Currency currency) {
    }
}
