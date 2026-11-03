package com.example.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

/** Ajouter chaque nouvelle @Entity ici et incrementer `version` (+ migration). */
@Database(entities = [NoteEntity::class], version = 1, exportSchema = true)
abstract class AppDatabase : RoomDatabase() {
    abstract fun noteDao(): NoteDao
}
