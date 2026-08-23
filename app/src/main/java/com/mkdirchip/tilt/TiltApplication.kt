package com.mkdirchip.tilt

import android.app.Application
import android.content.Context
import com.mkdirchip.tilt.data.TilRepository
import com.mkdirchip.tilt.data.TiltDatabase
import com.mkdirchip.tilt.widget.refreshTiltWidgets

/**
 * Holds the database and repository for the whole process. Deliberately hand-rolled rather than
 * a DI framework: the Glance widget runs outside the activity and ViewModel graph, and reaching
 * the repository from there is a property access here instead of an entry-point lookup.
 */
class TiltApplication : Application() {

    val database: TiltDatabase by lazy { TiltDatabase.build(this) }

    val repository: TilRepository by lazy {
        TilRepository(
            dao = database.tilDao(),
            onDataChanged = { refreshTiltWidgets(this) },
        )
    }
}

/** The container, from anywhere holding a [Context] — including the widget. */
val Context.tiltContainer: TiltApplication
    get() = applicationContext as TiltApplication
