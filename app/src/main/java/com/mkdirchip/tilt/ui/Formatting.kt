package com.mkdirchip.tilt.ui

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

private val cardStampFormatter: DateTimeFormatter =
    DateTimeFormatter.ofPattern("EEE d MMM · h:mm a", Locale.getDefault())

/** The date and time shown at a card's foot, e.g. "Sun 23 Aug · 8:04 PM". */
fun formatCardStamp(epochMillis: Long, zone: ZoneId = ZoneId.systemDefault()): String =
    Instant.ofEpochMilli(epochMillis).atZone(zone).format(cardStampFormatter)

private val widgetStampFormatter: DateTimeFormatter =
    DateTimeFormatter.ofPattern("d MMM yyyy", Locale.getDefault())

/** The shorter stamp the widget shows under a resurfaced entry. */
fun formatWidgetStamp(epochMillis: Long, zone: ZoneId = ZoneId.systemDefault()): String =
    Instant.ofEpochMilli(epochMillis).atZone(zone).format(widgetStampFormatter)
