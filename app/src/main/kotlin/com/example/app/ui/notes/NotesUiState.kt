package com.example.app.ui.notes

import com.example.app.domain.model.Note

/** Etat COMPLET de l'ecran, immuable. L'ecran n'affiche que ce qui est ici. */
data class NotesUiState(
    val notes: List<Note> = emptyList(),
    val draft: String = "",
    val isLoading: Boolean = true,
    val error: NotesError? = null
)

/** Erreurs metier sans Context : l'ecran les traduit en String via stringResource. */
enum class NotesError { BlankTitle }

/** Tout ce que l'utilisateur peut faire sur l'ecran. */
sealed interface NotesEvent {
    data class DraftChanged(val text: String) : NotesEvent

    data object AddClicked : NotesEvent

    data class DeleteClicked(val id: Long) : NotesEvent
}
