package com.mkdirchip.tilt.ui.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.mkdirchip.tilt.data.TilRepository
import com.mkdirchip.tilt.ui.timeline.DateFilter
import com.mkdirchip.tilt.ui.timeline.resolveBounds
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth

data class PeriodSummary(
    val entries: Int = 0,
    val activeDays: Int = 0,
)

data class StatsUiState(
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val week: PeriodSummary = PeriodSummary(),
    val month: PeriodSummary = PeriodSummary(),
    val countsByDate: Map<LocalDate, Int> = emptyMap(),
    val visibleMonth: YearMonth = YearMonth.now(),
    val today: LocalDate = LocalDate.now(),
    val hasAnyEntries: Boolean = false,
) {
    /** Share of days so far this month that hold an entry — what the ring shows. */
    val monthProgress: Float
        get() {
            val elapsed = if (visibleMonth == YearMonth.from(today)) {
                today.dayOfMonth
            } else {
                visibleMonth.lengthOfMonth()
            }
            val active = countsByDate.keys.count { YearMonth.from(it) == visibleMonth }
            return if (elapsed == 0) 0f else (active.toFloat() / elapsed).coerceIn(0f, 1f)
        }
}

class StatsViewModel(
    repository: TilRepository,
    weekStart: DayOfWeek,
    private val today: () -> LocalDate = { LocalDate.now() },
) : ViewModel() {

    private val visibleMonth = MutableStateFlow(YearMonth.from(today()))

    private val weekBounds = resolveBounds(DateFilter.ThisWeek, today(), weekStart)
    private val monthBounds = resolveBounds(DateFilter.ThisMonth, today(), weekStart)

    val state: StateFlow<StatsUiState> = combine(
        repository.observeDateCounts(),
        repository.observeEntryCount(weekBounds.start!!, weekBounds.end!!),
        repository.observeActiveDayCount(weekBounds.start, weekBounds.end),
        repository.observeEntryCount(monthBounds.start!!, monthBounds.end!!),
        repository.observeActiveDayCount(monthBounds.start, monthBounds.end),
        visibleMonth,
    ) { values ->
        @Suppress("UNCHECKED_CAST")
        val counts = values[0] as List<com.mkdirchip.tilt.data.DateCount>
        val weekEntries = values[1] as Int
        val weekDays = values[2] as Int
        val monthEntries = values[3] as Int
        val monthDays = values[4] as Int
        val month = values[5] as YearMonth

        val byDate = counts.mapNotNull { row ->
            runCatching { LocalDate.parse(row.localDate) to row.count }.getOrNull()
        }.toMap()

        val now = today()
        StatsUiState(
            currentStreak = currentStreak(byDate.keys, now),
            longestStreak = longestStreak(byDate.keys),
            week = PeriodSummary(weekEntries, weekDays),
            month = PeriodSummary(monthEntries, monthDays),
            countsByDate = byDate,
            visibleMonth = month,
            today = now,
            hasAnyEntries = byDate.isNotEmpty(),
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = StatsUiState(visibleMonth = YearMonth.from(today()), today = today()),
    )

    fun showPreviousMonth() {
        visibleMonth.value = visibleMonth.value.minusMonths(1)
    }

    fun showNextMonth() {
        visibleMonth.value = visibleMonth.value.plusMonths(1)
    }

    companion object {
        fun factory(repository: TilRepository, weekStart: DayOfWeek) = viewModelFactory {
            initializer { StatsViewModel(repository, weekStart) }
        }
    }
}
