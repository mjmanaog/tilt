package com.mkdirchip.tilt.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LabelNormalizerTest {

    @Test
    fun `labels differing only by case share a key`() {
        assertEquals(normalizeLabel("science"), normalizeLabel("Science"))
        assertEquals(normalizeLabel("science"), normalizeLabel("SCIENCE"))
    }

    @Test
    fun `surrounding whitespace does not create a distinct label`() {
        assertEquals(normalizeLabel("music"), normalizeLabel("  music  "))
    }

    @Test
    fun `blank text is not a usable label`() {
        assertFalse(isLabelUsable(""))
        assertFalse(isLabelUsable("   "))
        assertTrue(isLabelUsable("general"))
    }

    @Test
    fun `duplicate labels on one entry collapse to a single label`() {
        val labels = normalizeLabels(listOf("science", "Science", " SCIENCE "))
        assertEquals(1, labels.size)
        assertEquals("science", labels.single().name)
    }

    @Test
    fun `first typed form is kept as the display name`() {
        val labels = normalizeLabels(listOf("Science", "science"))
        assertEquals("Science", labels.single().displayName)
        assertEquals("science", labels.single().name)
    }

    @Test
    fun `blanks are dropped from a batch`() {
        val labels = normalizeLabels(listOf("science", "", "   ", "music"))
        assertEquals(listOf("science", "music"), labels.map { it.name })
    }

    @Test
    fun `distinct labels are all kept in the order first seen`() {
        val labels = normalizeLabels(listOf("music", "science", "general"))
        assertEquals(listOf("music", "science", "general"), labels.map { it.name })
    }

    @Test
    fun `an empty batch yields no labels`() {
        assertTrue(normalizeLabels(emptyList()).isEmpty())
    }
}
