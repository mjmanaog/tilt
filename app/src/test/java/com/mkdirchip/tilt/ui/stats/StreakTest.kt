package com.mkdirchip.tilt.ui.stats

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class StreakTest {

    private val today = LocalDate.of(2026, 8, 23)

    private fun days(vararg dayOfMonth: Int): Set<LocalDate> =
        dayOfMonth.map { LocalDate.of(2026, 8, it) }.toSet()

    // ---- current streak ----------------------------------------------------

    @Test
    fun `run ending today counts through today`() {
        // 20, 21, 22, 23 — four consecutive days ending today.
        assertEquals(4, currentStreak(days(20, 21, 22, 23), today))
    }

    @Test
    fun `today not yet captured still counts the run ending yesterday`() {
        // 20, 21, 22 with nothing today: three days, not zero.
        assertEquals(3, currentStreak(days(20, 21, 22), today))
    }

    @Test
    fun `a missed day breaks the streak`() {
        // Most recent entry is the 20th; the 21st and 22nd are empty.
        assertEquals(0, currentStreak(days(18, 19, 20), today))
    }

    @Test
    fun `only today is a streak of one`() {
        assertEquals(1, currentStreak(days(23), today))
    }

    @Test
    fun `only yesterday is a streak of one`() {
        assertEquals(1, currentStreak(days(22), today))
    }

    @Test
    fun `no entries at all is zero`() {
        assertEquals(0, currentStreak(emptySet(), today))
    }

    @Test
    fun `a gap earlier in history does not extend the current run`() {
        // 15, 16 then a gap, then 22, 23. Only the recent pair counts.
        assertEquals(2, currentStreak(days(15, 16, 22, 23), today))
    }

    @Test
    fun `a streak can run across a month boundary`() {
        val dates = setOf(
            LocalDate.of(2026, 7, 30),
            LocalDate.of(2026, 7, 31),
            LocalDate.of(2026, 8, 1),
        )
        assertEquals(3, currentStreak(dates, LocalDate.of(2026, 8, 1)))
    }

    // ---- longest streak ----------------------------------------------------

    @Test
    fun `longest streak finds the best past run`() {
        // 1..5 is five days; 20,21 is two.
        assertEquals(5, longestStreak(days(1, 2, 3, 4, 5, 20, 21)))
    }

    @Test
    fun `longest streak survives the current streak being broken`() {
        val dates = days(1, 2, 3, 4, 5, 20)
        assertEquals(0, currentStreak(dates, today))
        assertEquals(5, longestStreak(dates))
    }

    @Test
    fun `longest streak of scattered single days is one`() {
        assertEquals(1, longestStreak(days(1, 5, 10, 20)))
    }

    @Test
    fun `longest streak with no entries is zero`() {
        assertEquals(0, longestStreak(emptySet()))
    }

    @Test
    fun `longest streak of a single day is one`() {
        assertEquals(1, longestStreak(days(7)))
    }

    // ---- parsing -----------------------------------------------------------

    @Test
    fun `several entries on one day count as one day`() {
        // The data layer stores dates, so duplicates collapse into the set.
        val parsed = parseActiveDates(listOf("2026-08-23", "2026-08-23", "2026-08-22"))
        assertEquals(2, parsed.size)
        assertEquals(2, currentStreak(parsed, today))
    }

    @Test
    fun `unparseable dates are ignored rather than fatal`() {
        val parsed = parseActiveDates(listOf("2026-08-23", "not-a-date", ""))
        assertEquals(setOf(LocalDate.of(2026, 8, 23)), parsed)
    }
}
