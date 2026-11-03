package com.example.app.di

import androidx.room.Room
import com.example.app.data.local.AppDatabase
import com.example.app.data.remote.createHttpClient
import com.example.app.data.repository.NotesRepository
import com.example.app.data.repository.RoomNotesRepository
import com.example.app.ui.about.AboutViewModel
import com.example.app.ui.notes.NotesViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

/** Toute nouvelle classe injectee est declaree ICI (une ligne par classe). */
val appModule = module {
    // Prototype : schema change => base recreee. A remplacer par de vraies migrations avant publication.
    single {
        Room.databaseBuilder(androidContext(), AppDatabase::class.java, "app.db")
            .fallbackToDestructiveMigration(dropAllTables = true)
            .build()
    }
    single { get<AppDatabase>().noteDao() }
    single { createHttpClient() }

    singleOf(::RoomNotesRepository) { bind<NotesRepository>() }

    viewModelOf(::NotesViewModel)
    viewModelOf(::AboutViewModel)
}
