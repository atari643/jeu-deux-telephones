package com.example.app.data.repository

import com.example.app.data.local.NoteDao
import com.example.app.data.local.NoteEntity
import com.example.app.data.local.toDomain
import com.example.app.domain.model.Note
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/** Le ViewModel ne connait QUE cette interface (facile a remplacer par un fake en test). */
interface NotesRepository {
    fun observeNotes(): Flow<List<Note>>

    suspend fun addNote(title: String)

    suspend fun deleteNote(id: Long)
}

class RoomNotesRepository(private val dao: NoteDao) : NotesRepository {
    override fun observeNotes(): Flow<List<Note>> = dao.observeAll().map { list -> list.map { it.toDomain() } }

    override suspend fun addNote(title: String) {
        dao.insert(NoteEntity(title = title, createdAt = System.currentTimeMillis()))
    }

    override suspend fun deleteNote(id: Long) {
        dao.deleteById(id)
    }
}
