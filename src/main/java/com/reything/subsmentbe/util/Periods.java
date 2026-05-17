package com.reything.subsmentbe.util;

import com.reything.subsmentbe.domain.enums.Period;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

public final class Periods {

    private static final BigDecimal WEEKLY_MULTIPLIER = new BigDecimal("4.33");
    private static final BigDecimal MONTHS_PER_YEAR = new BigDecimal("12");

    private Periods() {
    }

    public static LocalDate calculateNext(LocalDate from, Period period) {
        if (from == null || period == null) return from;
        LocalDate today = LocalDate.now();
        LocalDate next = from;
        switch (period) {
            case monthly -> {
                while (!next.isAfter(today)) next = next.plusMonths(1);
            }
            case yearly -> {
                while (!next.isAfter(today)) next = next.plusYears(1);
            }
            case weekly -> {
                while (!next.isAfter(today)) next = next.plusWeeks(1);
            }
            case custom -> {
                return from;
            }
        }
        return next;
    }

    public static BigDecimal monthlyEquivalent(BigDecimal amount, Period period) {
        if (amount == null || period == null) return BigDecimal.ZERO;
        return switch (period) {
            case monthly -> amount;
            case yearly -> amount.divide(MONTHS_PER_YEAR, 4, RoundingMode.HALF_UP);
            case weekly -> amount.multiply(WEEKLY_MULTIPLIER);
            case custom -> BigDecimal.ZERO;
        };
    }
}
