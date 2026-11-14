package com.example.app.di

import androidx.room.Room
import com.example.app.data.link.InMemoryGameLinkFactory
import com.example.app.data.link.InMemoryGameSession
import com.example.app.data.local.AppDatabase
import com.example.app.data.remote.createHttpClient
import com.example.app.data.repository.NotesRepository
import com.example.app.data.repository.RoomNotesRepository
import com.example.app.data.sensors.InMemorySensorSource
import com.example.app.game.contract.Engine
import com.example.app.game.contract.GameLinkFactory
import com.example.app.game.contract.GameSession
import com.example.app.game.contract.InputSource
import com.example.app.game.contract.SensorSource
import com.example.app.game.engine.GameEngine
import com.example.app.game.input.InMemoryInputSource
import com.example.app.ui.about.AboutViewModel
import com.example.app.ui.game.GameViewModel
import com.example.app.ui.lobby.LobbyViewModel
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

    // --- A : moteur, ecran de jeu (implementations de depart, CA7) ---
    singleOf(::GameEngine) { bind<Engine>() }
    viewModelOf(::GameViewModel)

    // --- B : capteurs, liaison, entrees, lobby (implementations de depart, CA7) ---
    singleOf(::InMemoryGameLinkFactory) { bind<GameLinkFactory>() }
    singleOf(::InMemoryGameSession) { bind<GameSession>() }
    singleOf(::InMemorySensorSource) { bind<SensorSource>() }
    singleOf(::InMemoryInputSource) { bind<InputSource>() }
    viewModelOf(::LobbyViewModel)
}
