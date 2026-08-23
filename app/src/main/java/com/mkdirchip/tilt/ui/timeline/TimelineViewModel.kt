package com.mkdirchip.tilt.ui.timeline

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.mkdirchip.tilt.data.LabelEntity
import com.mkdirchip.tilt.data.TilEntryWithLabels
import com.mkdirchip.tilt.data.TilRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate

data class TimelineUiState(
    val entries: List<TilEntryWithLabels> = emptyList(),
    val labels: List<LabelEntity> = emptyList(),
    val dateFilter: DateFilter = DateFilter.All,
    val selectedLabelIds: Set<Long> = emptySet(),
    val loading: Boolean = true,
) {
    val filtersActive: Boolean
        get() = dateFilter !is DateFilter.All || selectedLabelIds.isNotEmpty()
}

private data class Selection(
    val dateFilter: DateFilter,
    val labelIds: Set<Long>,
)

/**
 * Holds the timeline's filter selection. Living here — rather than in saved state or on disk —
 * is exactly why filters survive navigating away and back, yet reset to All on a cold start.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class TimelineViewModel(
    private val repository: TilRepository,
    private val weekStart: DayOfWeek,
    private val today: () -> LocalDate = { LocalDate.now() },
) : ViewModel() {

    private val selection = MutableStateFlow(Selection(DateFilter.All, emptySet()))

    private val entries = selection.flatMapLatest { current ->
        val bounds = resolveBounds(current.dateFilter, today(), weekStart)
        repository.observeEntries(bounds.start, bounds.end, current.labelIds.toList())
    }

    val state: StateFlow<TimelineUiState> = combine(
        entries,
        repository.observeLabelsInUse(),
        selection,
    ) { entries, labels, current ->
        TimelineUiState(
            entries = entries,
            labels = labels,
            dateFilter = current.dateFilter,
            selectedLabelIds = current.labelIds,
            loading = false,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = TimelineUiState(),
    )

    /** True when nothing at all has been captured, as distinct from filters excluding everything. */
    val hasAnyEntries: StateFlow<Boolean> = repository.observeEntries()
        .map { it.isNotEmpty() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    fun setDateFilter(filter: DateFilter) = selection.update { it.copy(dateFilter = filter) }

    fun toggleLabel(labelId: Long) = selection.update { current ->
        val ids = if (labelId in current.labelIds) {
            current.labelIds - labelId
        } else {
            current.labelIds + labelId
        }
        current.copy(labelIds = ids)
    }

    fun clearFilters() = selection.update { Selection(DateFilter.All, emptySet()) }

    // ---- entry actions -----------------------------------------------------

    private val _lastDeleted = MutableStateFlow<TilEntryWithLabels?>(null)
    val lastDeleted: StateFlow<TilEntryWithLabels?> = _lastDeleted

    fun delete(item: TilEntryWithLabels) {
        viewModelScope.launch {
            _lastDeleted.value = item
            repository.delete(item.entry)
        }
    }

    /** Puts the entry back as the same entry: same id, timestamp, and labels. */
    fun undoDelete() {
        val snapshot = _lastDeleted.value ?: return
        viewModelScope.launch {
            repository.restore(snapshot)
            _lastDeleted.value = null
        }
    }

    fun forgetDeleted() {
        _lastDeleted.value = null
    }

    companion object {
        fun factory(repository: TilRepository, weekStart: DayOfWeek) = viewModelFactory {
            initializer { TimelineViewModel(repository, weekStart) }
        }
    }
}
