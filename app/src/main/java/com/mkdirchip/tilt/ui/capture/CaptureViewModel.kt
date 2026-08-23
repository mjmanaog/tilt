package com.mkdirchip.tilt.ui.capture

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.mkdirchip.tilt.data.TilEntryEntity
import com.mkdirchip.tilt.data.TilRepository
import com.mkdirchip.tilt.data.isLabelUsable
import com.mkdirchip.tilt.data.normalizeLabel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CaptureUiState(
    val text: String = "",
    val labelDraft: String = "",
    val labels: List<String> = emptyList(),
    val suggestions: List<String> = emptyList(),
    val isEditing: Boolean = false,
    val loaded: Boolean = false,
) {
    /** Blank or whitespace-only text cannot be saved. */
    val canSave: Boolean get() = text.isNotBlank()
}

/**
 * Backs both capture surfaces — the in-app editor and the widget's overlay — so the two cannot
 * drift apart on validation or label handling. [entryId] null means a new entry.
 */
class CaptureViewModel(
    private val repository: TilRepository,
    private val entryId: Long?,
) : ViewModel() {

    private val _state = MutableStateFlow(CaptureUiState(isEditing = entryId != null))
    val state: StateFlow<CaptureUiState> = _state.asStateFlow()

    private var existing: TilEntryEntity? = null
    private var knownLabels: List<String> = emptyList()

    init {
        viewModelScope.launch {
            if (entryId != null) {
                repository.entry(entryId)?.let { loaded ->
                    existing = loaded.entry
                    _state.update {
                        val labels = loaded.labels.map { label -> label.displayName }
                        it.copy(
                            text = loaded.entry.text,
                            labels = labels,
                            // Recomputed here too: the label flow may have produced suggestions
                            // before this entry finished loading, which would leave the entry's
                            // own labels sitting in its suggestion row.
                            suggestions = suggestionsFor(it.labelDraft, labels),
                        )
                    }
                }
            }
            _state.update { it.copy(loaded = true) }
        }
        viewModelScope.launch {
            repository.observeLabelsInUse().collect { labels ->
                knownLabels = labels.map { it.displayName }
                _state.update { it.copy(suggestions = suggestionsFor(it.labelDraft, it.labels)) }
            }
        }
    }

    fun onTextChange(value: String) = _state.update { it.copy(text = value) }

    fun onLabelDraftChange(value: String) = _state.update {
        it.copy(labelDraft = value, suggestions = suggestionsFor(value, it.labels))
    }

    /** Attaches the draft (or a tapped suggestion) as a label, ignoring blanks and duplicates. */
    fun addLabel(raw: String) {
        if (!isLabelUsable(raw)) return
        _state.update { current ->
            val key = normalizeLabel(raw)
            val alreadyThere = current.labels.any { normalizeLabel(it) == key }
            val labels = if (alreadyThere) current.labels else current.labels + raw.trim()
            current.copy(labels = labels, labelDraft = "", suggestions = suggestionsFor("", labels))
        }
    }

    fun removeLabel(label: String) = _state.update { current ->
        val labels = current.labels.filterNot { normalizeLabel(it) == normalizeLabel(label) }
        current.copy(labels = labels, suggestions = suggestionsFor(current.labelDraft, labels))
    }

    /** Saves, then calls [onSaved]. A label still sitting in the draft field is not lost. */
    fun save(onSaved: () -> Unit) {
        val current = _state.value
        if (!current.canSave) return
        val labels = if (isLabelUsable(current.labelDraft)) {
            val key = normalizeLabel(current.labelDraft)
            if (current.labels.none { normalizeLabel(it) == key }) {
                current.labels + current.labelDraft.trim()
            } else {
                current.labels
            }
        } else {
            current.labels
        }
        viewModelScope.launch {
            val target = existing
            if (target != null) {
                repository.edit(target, current.text, labels)
            } else {
                repository.capture(current.text, labels)
            }
            onSaved()
        }
    }

    private fun suggestionsFor(draft: String, attached: List<String>): List<String> {
        val attachedKeys = attached.map { normalizeLabel(it) }.toSet()
        val prefix = normalizeLabel(draft)
        return knownLabels
            .filter { normalizeLabel(it) !in attachedKeys }
            .filter { prefix.isEmpty() || normalizeLabel(it).startsWith(prefix) }
            .take(6)
    }

    companion object {
        fun factory(repository: TilRepository, entryId: Long?) = viewModelFactory {
            initializer { CaptureViewModel(repository, entryId) }
        }
    }
}
