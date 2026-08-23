package com.mkdirchip.tilt.data

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.Junction
import androidx.room.PrimaryKey
import androidx.room.Relation

/**
 * A thing learned. [localDate] is the local calendar date at the moment of capture, stored
 * alongside the instant so that travelling between timezones cannot retroactively move an
 * entry to a different day (and so quietly rewrite streaks).
 */
@Entity(tableName = "entries", indices = [Index("localDate")])
data class TilEntryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val text: String,
    val createdAtEpochMillis: Long,
    val localDate: String,
)

/**
 * A label. [name] is the normalized key — trimmed and lowercased — and carries the unique
 * index, so "Science" and "science" cannot both exist. [displayName] preserves the form the
 * user first typed.
 */
@Entity(tableName = "labels", indices = [Index(value = ["name"], unique = true)])
data class LabelEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val displayName: String,
)

@Entity(
    tableName = "entry_labels",
    primaryKeys = ["entryId", "labelId"],
    foreignKeys = [
        ForeignKey(
            entity = TilEntryEntity::class,
            parentColumns = ["id"],
            childColumns = ["entryId"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = LabelEntity::class,
            parentColumns = ["id"],
            childColumns = ["labelId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("labelId")],
)
data class EntryLabelCrossRef(
    val entryId: Long,
    val labelId: Long,
)

/** How many entries fall on one local date; drives the calendar's per-day weight. */
data class DateCount(
    val localDate: String,
    val count: Int,
)

/** An entry together with the labels attached to it. */
data class TilEntryWithLabels(
    @Embedded val entry: TilEntryEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = EntryLabelCrossRef::class,
            parentColumn = "entryId",
            entityColumn = "labelId",
        ),
    )
    val labels: List<LabelEntity>,
)
