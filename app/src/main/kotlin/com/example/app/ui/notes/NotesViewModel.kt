package com.example.app.ui.notes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.app.data.repository.NotesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val STOP_TIMEOUT_MS = 5_000L

class NotesViewModel(private val repository: NotesRepository) : ViewModel() {
    private val form = MutableStateFlow(FormState())

    val uiState: StateFlow<NotesUiState> =
        combine(repository.observeNotes(), form) { notes, f ->
            NotesUiState(notes = notes, draft = f.draft, isLoading = false, error = f.error)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), NotesUiState())

    fun onEvent(event: NotesEvent) {
        when (event) {
            is NotesEvent.DraftChanged -> form.update { it.copy(draft = event.text, error = null) }
            NotesEvent.AddClicked -> addNote()
            is NotesEvent.DeleteClicked -> viewModelScope.launch { repository.deleteNote(event.id) }
        }
    }

    private fun addNote() {
        val title = form.value.draft.trim()
        if (title.isEmpty()) {
            form.update { it.copy(error = NotesError.BlankTitle) }
            return
        }
        viewModelScope.launch {
            repository.addNote(title)
            form.update { FormState() }
        }
    }

    private data class FormState(val draft: String = "", val error: NotesError? = null)
}
