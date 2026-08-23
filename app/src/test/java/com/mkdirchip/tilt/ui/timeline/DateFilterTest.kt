package com.mkdirchip.tilt.ui.timeline

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalDate

class DateFilterTest {

    // Sunday 23 August 2026.
    private val today = LocalDate.of(2026, 8, 23)

    @Test
    fun `all is unbounded in both directions`() {
        val bounds = resolveBounds(DateFilter.All, today, DayOfWeek.MONDAY)
        assertNull(bounds.start)
        assertNull(bounds.end)
    }

    @Test
    fun `today is a single day`() {
        val bounds = resolveBounds(DateFilter.Today, today, DayOfWeek.MONDAY)
        assertEquals("2026-08-23", bounds.start)
        assertEquals("2026-08-23", bounds.end)
    }

    @Test
    fun `week honours a Monday week start`() {
        val bounds = resolveBounds(DateFilter.ThisWeek, today, DayOfWeek.MONDAY)
        // Sunday belongs to the week that began Monday the 17th.
        assertEquals("2026-08-17", bounds.start)
        assertEquals("2026-08-23", bounds.end)
    }

    @Test
    fun `week honours a Sunday week start`() {
        val bounds = resolveBounds(DateFilter.ThisWeek, today, DayOfWeek.SUNDAY)
        // With Sunday first, today opens the week.
        assertEquals("2026-08-23", bounds.start)
        assertEquals("2026-08-29", bounds.end)
    }

    @Test
    fun `week start on a Saturday locale`() {
        val bounds = resolveBounds(DateFilter.ThisWeek, today, DayOfWeek.SATURDAY)
        assertEquals("2026-08-22", bounds.start)
        assertEquals("2026-08-28", bounds.end)
    }

    @Test
    fun `month spans the whole calendar month`() {
        val bounds = resolveBounds(DateFilter.ThisMonth, today, DayOfWeek.MONDAY)
        assertEquals("2026-08-01", bounds.start)
        assertEquals("2026-08-31", bounds.end)
    }

    @Test
    fun `month handles February in a non-leap year`() {
        val bounds = resolveBounds(
            DateFilter.ThisMonth,
            LocalDate.of(2026, 2, 10),
            DayOfWeek.MONDAY,
        )
        assertEquals("2026-02-01", bounds.start)
        assertEquals("2026-02-28", bounds.end)
    }

    @Test
    fun `month handles February in a leap year`() {
        val bounds = resolveBounds(
            DateFilter.ThisMonth,
            LocalDate.of(2028, 2, 10),
            DayOfWeek.MONDAY,
        )
        assertEquals("2028-02-29", bounds.end)
    }

    @Test
    fun `custom range keeps both endpoints`() {
        val bounds = resolveBounds(
            DateFilter.Custom(LocalDate.of(2026, 8, 6), LocalDate.of(2026, 8, 9)),
            today,
            DayOfWeek.MONDAY,
        )
        assertEquals("2026-08-06", bounds.start)
        assertEquals("2026-08-09", bounds.end)
    }

    @Test
    fun `custom range of one day is that day`() {
        val day = LocalDate.of(2026, 8, 7)
        val bounds = resolveBounds(DateFilter.Custom(day, day), today, DayOfWeek.MONDAY)
        assertEquals("2026-08-07", bounds.start)
        assertEquals("2026-08-07", bounds.end)
    }

    @Test
    fun `a reversed custom range is put back in order`() {
        val bounds = resolveBounds(
            DateFilter.Custom(LocalDate.of(2026, 8, 9), LocalDate.of(2026, 8, 6)),
            today,
            DayOfWeek.MONDAY,
        )
        assertEquals("2026-08-06", bounds.start)
        assertEquals("2026-08-09", bounds.end)
    }

    @Test
    fun `week spanning a month boundary is contiguous`() {
        val bounds = resolveBounds(
            DateFilter.ThisWeek,
            LocalDate.of(2026, 9, 2),
            DayOfWeek.MONDAY,
        )
        assertEquals("2026-08-31", bounds.start)
        assertEquals("2026-09-06", bounds.end)
    }
}
