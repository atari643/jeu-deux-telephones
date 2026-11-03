package com.example.app.testing

import com.example.app.data.repository.NotesRepository
import com.example.app.domain.model.Note
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

/** Fake en memoire : prefere aux mocks pour les repositories. Un fake par interface. */
class FakeNotesRepository(initial: List<Note> = emptyList()) : NotesRepository {
    private val notes = MutableStateFlow(initial)
    private var nextId = (initial.maxOfOrNull { it.id } ?: 0) + 1

    override fun observeNotes(): Flow<List<Note>> = notes

    override suspend fun addNote(title: String) {
        notes.update { it + Note(id = nextId++, title = title, createdAt = 0) }
    }

    override suspend fun deleteNote(id: Long) {
        notes.update { list -> list.filterNot { it.id == id } }
    }
}
