package com.mkdirchip.tilt.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [TilEntryEntity::class, LabelEntity::class, EntryLabelCrossRef::class],
    version = 1,
    exportSchema = true,
)
abstract class TiltDatabase : RoomDatabase() {

    abstract fun tilDao(): TilDao

    companion object {
        fun build(context: Context): TiltDatabase =
            Room.databaseBuilder(context, TiltDatabase::class.java, "tilt.db")
                .build()
    }
}
