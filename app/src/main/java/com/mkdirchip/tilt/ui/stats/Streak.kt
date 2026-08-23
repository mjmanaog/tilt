package com.mkdirchip.tilt.ui.stats

import java.time.LocalDate

/**
 * The run of consecutive days ending today, or ending yesterday when today holds no entry yet.
 *
 * That grace day matters: without it an unbroken habit reads as a streak of zero all morning,
 * which is precisely when the number is discouraging rather than motivating.
 */
fun currentStreak(activeDates: Set<LocalDate>, today: LocalDate): Int {
    val anchor = when {
        today in activeDates -> today
        today.minusDays(1) in activeDates -> today.minusDays(1)
        else -> return 0
    }

    var streak = 0
    var day = anchor
    while (day in activeDates) {
        streak++
        day = day.minusDays(1)
    }
    return streak
}

/** The longest run of consecutive active days ever recorded. */
fun longestStreak(activeDates: Set<LocalDate>): Int {
    if (activeDates.isEmpty()) return 0

    val sorted = activeDates.sorted()
    var longest = 1
    var run = 1
    for (i in 1 until sorted.size) {
        run = if (sorted[i - 1].plusDays(1) == sorted[i]) run + 1 else 1
        longest = maxOf(longest, run)
    }
    return longest
}

/** Parses stored `yyyy-MM-dd` values, ignoring anything unparseable rather than crashing. */
fun parseActiveDates(raw: Collection<String>): Set<LocalDate> =
    raw.mapNotNullTo(mutableSetOf()) { runCatching { LocalDate.parse(it) }.getOrNull() }
