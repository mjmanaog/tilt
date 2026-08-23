package com.mkdirchip.tilt.data

import java.util.Locale

/**
 * Turns raw typed label text into a normalized key. Trimming and lowercasing here is what
 * makes "Science" and " science " the same label, enforced by the unique index on
 * [LabelEntity.name] rather than by remembering to check at every write site.
 */
fun normalizeLabel(raw: String): String =
    raw.trim().lowercase(Locale.ROOT)

/** True when the text would produce a usable label. */
fun isLabelUsable(raw: String): Boolean = normalizeLabel(raw).isNotEmpty()

/**
 * Normalizes a batch of typed labels, dropping blanks and collapsing duplicates while
 * keeping the first-typed display form for each distinct label.
 */
fun normalizeLabels(raw: List<String>): List<LabelEntity> {
    val seen = LinkedHashMap<String, String>()
    for (candidate in raw) {
        val key = normalizeLabel(candidate)
        if (key.isEmpty()) continue
        seen.putIfAbsent(key, candidate.trim())
    }
    return seen.map { (key, display) -> LabelEntity(name = key, displayName = display) }
}
