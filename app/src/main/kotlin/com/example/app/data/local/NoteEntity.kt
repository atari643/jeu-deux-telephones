package com.example.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.app.domain.model.Note

@Entity(tableName = "notes")
data class NoteEntity(@PrimaryKey(autoGenerate = true) val id: Long = 0, val title: String, val createdAt: Long)

fun NoteEntity.toDomain(): Note = Note(id = id, title = title, createdAt = createdAt)
