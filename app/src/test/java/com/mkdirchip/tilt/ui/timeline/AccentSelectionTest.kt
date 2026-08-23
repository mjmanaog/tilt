package com.mkdirchip.tilt.ui.timeline

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AccentSelectionTest {

    @Test
    fun `the same id always gets the same treatment`() {
        for (id in 1L..500L) {
            assertEquals(
                "id $id changed its mind",
                isAccentCard(id),
                isAccentCard(id),
            )
        }
    }

    @Test
    fun `roughly one card in five is accented`() {
        val accented = (1L..2000L).count { isAccentCard(it) }
        // 1-in-5 is 400; allow generous slack for hash distribution.
        assertTrue("got $accented of 2000", accented in 300..500)
    }

    @Test
    fun `both treatments occur among the first handful of entries`() {
        val treatments = (1L..20L).map { isAccentCard(it) }.toSet()
        assertEquals(setOf(true, false), treatments)
    }

    @Test
    fun `consecutive ids do not form a long run`() {
        var longestRun = 1
        var run = 1
        for (id in 2L..1000L) {
            if (isAccentCard(id) == isAccentCard(id - 1)) run++ else run = 1
            longestRun = maxOf(longestRun, run)
        }
        // A visible stripe of identical treatment would defeat the point of the scatter.
        assertTrue("longest run was $longestRun", longestRun < 40)
    }

    @Test
    fun `zero id is handled`() {
        isAccentCard(0L)
    }
}
