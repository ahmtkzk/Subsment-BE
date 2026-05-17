package com.reything.subsmentbe.dto.calendar;

import java.util.List;

public record CalendarDay(
        int day,
        List<CalendarSubscription> subscriptions
) {
}
