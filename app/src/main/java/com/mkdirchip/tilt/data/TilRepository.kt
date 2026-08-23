package com.mkdirchip.tilt.data

import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

/**
 * The single write path shared by the in-app editor and the widget capture surface, and the
 * single read path behind the timeline, the statistics, and the widget. Queries are returned as
 * flows so a write from any surface propagates without anyone having to notify anyone.
 */
class TilRepository(
    private val dao: TilDao,
    /**
     * Invoked after every write. The widget is refreshed through this rather than by the
     * repository importing the widget, which would point the data layer at the UI.
     */
    private val onDataChanged: suspend () -> Unit = {},
) {

    // ---- reads -------------------------------------------------------------

    fun observeEntries(
        start: String? = null,
        end: String? = null,
        labelIds: List<Long> = emptyList(),
    ): Flow<List<TilEntryWithLabels>> =
        if (labelIds.isEmpty()) {
            dao.observeEntries(start, end)
        } else {
            dao.observeEntriesWithAnyLabel(start, end, labelIds)
        }

    fun observeLabelsInUse(): Flow<List<LabelEntity>> = dao.observeLabelsInUse()

    fun observeActiveDates(): Flow<List<String>> = dao.observeActiveDates()

    fun observeDateCounts(): Flow<List<DateCount>> = dao.observeDateCounts()

    fun observeEntryCount(start: String, end: String): Flow<Int> = dao.observeEntryCount(start, end)

    fun observeActiveDayCount(start: String, end: String): Flow<Int> =
        dao.observeActiveDayCount(start, end)

    suspend fun entry(id: Long): TilEntryWithLabels? = dao.findEntry(id)

    suspend fun randomEntry(): TilEntryEntity? = dao.randomEntry()

    suspend fun hasEntries(): Boolean = dao.entryCount() > 0

    // ---- writes ------------------------------------------------------------

    /**
     * Records a new entry. Surrounding whitespace is dropped while interior line breaks are
     * kept as typed, and the capture instant and local capture date are stamped here.
     */
    suspend fun capture(text: String, rawLabels: List<String> = emptyList()): Long {
        val cleaned = text.trim()
        require(cleaned.isNotEmpty()) { "An entry needs text" }
        val entry = TilEntryEntity(
            text = cleaned,
            createdAtEpochMillis = System.currentTimeMillis(),
            localDate = LocalDate.now().toString(),
        )
        return dao.insertEntryWithLabels(entry, normalizeLabels(rawLabels)).also { onDataChanged() }
    }

    /** Edits an entry's text and labels. The original capture timestamp is left untouched. */
    suspend fun edit(existing: TilEntryEntity, text: String, rawLabels: List<String>) {
        val cleaned = text.trim()
        require(cleaned.isNotEmpty()) { "An entry needs text" }
        dao.updateEntryWithLabels(
            existing.copy(text = cleaned),
            normalizeLabels(rawLabels),
        )
        onDataChanged()
    }

    suspend fun delete(entry: TilEntryEntity) {
        dao.deleteEntryAndPruneLabels(entry)
        onDataChanged()
    }

    /** Puts back a deleted entry as the same entry: same id, timestamp, date, and labels. */
    suspend fun restore(snapshot: TilEntryWithLabels) {
        dao.insertEntryWithLabels(
            snapshot.entry,
            snapshot.labels.map { LabelEntity(name = it.name, displayName = it.displayName) },
        )
        onDataChanged()
    }
}
