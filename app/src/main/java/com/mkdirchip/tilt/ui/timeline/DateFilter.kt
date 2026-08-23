package com.mkdirchip.tilt.ui.timeline

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.WeekFields
import java.util.Locale

/** The date window the feed is narrowed to. */
sealed interface DateFilter {
    data object All : DateFilter
    data object Today : DateFilter
    data object ThisWeek : DateFilter
    data object ThisMonth : DateFilter
    data class Custom(val start: LocalDate, val end: LocalDate) : DateFilter
}

/**
 * Inclusive ISO-date bounds, or null for unbounded. These compare directly against the stored
 * `localDate` column, which is why bucketing needs no date arithmetic in SQL.
 */
data class DateBounds(val start: String?, val end: String?)

/** The locale's first day of week, which is what week-based presets are measured from. */
fun localeWeekStart(locale: Locale = Locale.getDefault()): DayOfWeek =
    WeekFields.of(locale).firstDayOfWeek

/**
 * Resolves a filter to concrete bounds. Pure: [today] and [weekStart] are passed in so every
 * boundary case is testable without touching the clock or the device locale.
 */
fun resolveBounds(
    filter: DateFilter,
    today: LocalDate,
    weekStart: DayOfWeek,
): DateBounds = when (filter) {
    DateFilter.All -> DateBounds(null, null)

    DateFilter.Today -> DateBounds(today.toString(), today.toString())

    DateFilter.ThisWeek -> {
        val daysSinceStart = (today.dayOfWeek.value - weekStart.value + 7) % 7
        val start = today.minusDays(daysSinceStart.toLong())
        DateBounds(start.toString(), start.plusDays(6).toString())
    }

    DateFilter.ThisMonth -> {
        val start = today.withDayOfMonth(1)
        DateBounds(start.toString(), start.plusMonths(1).minusDays(1).toString())
    }

    // Tolerates a reversed pair rather than showing nothing.
    is DateFilter.Custom -> {
        val lower = if (filter.start.isAfter(filter.end)) filter.end else filter.start
        val upper = if (filter.start.isAfter(filter.end)) filter.start else filter.end
        DateBounds(lower.toString(), upper.toString())
    }
}
