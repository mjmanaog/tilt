package com.mkdirchip.tilt.data

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TilDaoTest {

    private lateinit var db: TiltDatabase
    private lateinit var dao: TilDao

    @Before
    fun setUp() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            TiltDatabase::class.java,
        ).build()
        dao = db.tilDao()
    }

    @After
    fun tearDown() = db.close()

    private suspend fun add(
        text: String,
        date: String,
        vararg labels: String,
        at: Long = 0L,
    ): Long = dao.insertEntryWithLabels(
        TilEntryEntity(text = text, createdAtEpochMillis = at, localDate = date),
        normalizeLabels(labels.toList()),
    )

    @Test
    fun unbounded_range_returns_everything_newest_first() = runBlocking {
        add("older", "2026-08-01", at = 1)
        add("newer", "2026-08-02", at = 2)

        val entries = dao.observeEntries(null, null).first()

        assertEquals(listOf("newer", "older"), entries.map { it.entry.text })
    }

    @Test
    fun range_bounds_are_inclusive_on_both_ends() = runBlocking {
        add("before", "2026-08-05")
        add("start", "2026-08-06")
        add("middle", "2026-08-07")
        add("end", "2026-08-09")
        add("after", "2026-08-10")

        val entries = dao.observeEntries("2026-08-06", "2026-08-09").first()

        assertEquals(setOf("start", "middle", "end"), entries.map { it.entry.text }.toSet())
    }

    @Test
    fun single_day_range_returns_only_that_day() = runBlocking {
        add("that day", "2026-08-07")
        add("other day", "2026-08-08")

        val entries = dao.observeEntries("2026-08-07", "2026-08-07").first()

        assertEquals(listOf("that day"), entries.map { it.entry.text })
    }

    @Test
    fun several_labels_match_entries_carrying_any_of_them() = runBlocking {
        add("a", "2026-08-07", "science")
        add("b", "2026-08-07", "music")
        add("c", "2026-08-07", "cooking")

        val labels = dao.observeLabelsInUse().first().associateBy { it.name }
        val ids = listOfNotNull(labels["science"]?.id, labels["music"]?.id)

        val entries = dao.observeEntriesWithAnyLabel(null, null, ids).first()

        assertEquals(setOf("a", "b"), entries.map { it.entry.text }.toSet())
    }

    @Test
    fun label_and_date_filters_must_both_hold() = runBlocking {
        add("in range with label", "2026-08-07", "science")
        add("in range wrong label", "2026-08-07", "music")
        add("out of range with label", "2026-08-20", "science")

        val science = dao.observeLabelsInUse().first().single { it.name == "science" }

        val entries = dao
            .observeEntriesWithAnyLabel("2026-08-01", "2026-08-10", listOf(science.id))
            .first()

        assertEquals(listOf("in range with label"), entries.map { it.entry.text })
    }

    @Test
    fun labels_differing_only_by_case_resolve_to_one_label() = runBlocking {
        add("first", "2026-08-07", "Science")
        add("second", "2026-08-08", "science")

        val labels = dao.observeLabelsInUse().first()
        assertEquals(1, labels.size)

        val entries = dao.observeEntriesWithAnyLabel(null, null, listOf(labels.single().id)).first()
        assertEquals(2, entries.size)
    }

    @Test
    fun a_label_stops_being_offered_once_nothing_carries_it() = runBlocking {
        val id = add("only user of the label", "2026-08-07", "science")
        assertEquals(1, dao.observeLabelsInUse().first().size)

        val entry = requireNotNull(dao.findEntry(id))
        dao.deleteEntryAndPruneLabels(entry.entry)

        assertTrue(dao.observeLabelsInUse().first().isEmpty())
    }

    @Test
    fun editing_labels_replaces_rather_than_accumulates() = runBlocking {
        val id = add("entry", "2026-08-07", "science")
        val entry = requireNotNull(dao.findEntry(id))

        dao.updateEntryWithLabels(entry.entry, normalizeLabels(listOf("music")))

        val reloaded = requireNotNull(dao.findEntry(id))
        assertEquals(listOf("music"), reloaded.labels.map { it.name })
        assertEquals(listOf("music"), dao.observeLabelsInUse().first().map { it.name })
    }

    @Test
    fun reinserting_a_deleted_entry_preserves_its_identity() = runBlocking {
        val id = add("learned something", "2026-08-07", "science", at = 1234L)
        val snapshot = requireNotNull(dao.findEntry(id))

        dao.deleteEntryAndPruneLabels(snapshot.entry)
        assertTrue(dao.observeEntries(null, null).first().isEmpty())

        dao.insertEntryWithLabels(snapshot.entry, snapshot.labels)

        val restored = requireNotNull(dao.findEntry(id))
        assertEquals("learned something", restored.entry.text)
        assertEquals(1234L, restored.entry.createdAtEpochMillis)
        assertEquals("2026-08-07", restored.entry.localDate)
        assertEquals(listOf("science"), restored.labels.map { it.name })
    }

    @Test
    fun counts_cover_only_the_requested_window() = runBlocking {
        add("one", "2026-08-07")
        add("two", "2026-08-07")
        add("three", "2026-08-09")
        add("outside", "2026-09-01")

        assertEquals(3, dao.observeEntryCount("2026-08-01", "2026-08-31").first())
        assertEquals(2, dao.observeActiveDayCount("2026-08-01", "2026-08-31").first())
    }

    @Test
    fun active_dates_are_distinct_and_ascending() = runBlocking {
        add("a", "2026-08-09")
        add("b", "2026-08-07")
        add("c", "2026-08-07")

        assertEquals(listOf("2026-08-07", "2026-08-09"), dao.observeActiveDates().first())
    }

    @Test
    fun random_entry_is_null_only_when_there_are_none() = runBlocking {
        assertEquals(null, dao.randomEntry())
        add("something", "2026-08-07")
        assertNotNull(dao.randomEntry())
    }
}
