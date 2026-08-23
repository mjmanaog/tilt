package com.mkdirchip.tilt.widget

import android.content.Context
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.GlanceAppWidgetReceiver

class TiltWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = TiltWidget()
}

/**
 * Refreshes every placed widget. Called by the repository after any write, so a save from either
 * surface — the app or the widget's own overlay — is reflected instead of leaving stale content.
 *
 * Note: broadcasting ACTION_APPWIDGET_UPDATE is not an option here. It is a protected broadcast
 * that only the system may send, so an app-sent one never reaches the receiver.
 */
suspend fun refreshTiltWidgets(context: Context) {
    val widget = TiltWidget()
    val ids = GlanceAppWidgetManager(context).getGlanceIds(TiltWidget::class.java)
    ids.forEach { widget.update(context, it) }
}
