package com.mkdirchip.tilt.ui.timeline

/** Roughly one card in five carries the accent fill. */
private const val ACCENT_EVERY = 5

/**
 * Whether an entry's card is accent-filled. A pure function of the entry's stored id, never a
 * random draw: the treatment has to survive scrolling, filtering, and restarts, and a card that
 * changes colour as you scroll past it reads as a rendering bug rather than as rhythm.
 *
 * The id is mixed first so that consecutive ids do not land in a visible stripe.
 */
fun isAccentCard(entryId: Long): Boolean {
    val mixed = entryId * -0x61c8864680b583ebL // 2^64 / golden ratio
    return ((mixed ushr 33) % ACCENT_EVERY) == 0L
}
