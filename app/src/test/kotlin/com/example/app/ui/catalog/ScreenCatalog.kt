package com.example.app.ui.catalog

import androidx.compose.runtime.Composable
import com.example.app.ui.about.AboutPreviewData
import com.example.app.ui.about.AboutScreen
import com.example.app.ui.notes.NotesPreviewData
import com.example.app.ui.notes.NotesScreen

/**
 * Catalogue des ecrans a "voir" (tools/see.ps1) et a photographier (Roborazzi).
 * NOUVEL ECRAN => ajouter ici une entree par etat interessant : "<Ecran>_<etat>" to { ... }.
 * Toujours l'ecran SANS etat + des donnees de preview, jamais de ViewModel ni de Koin.
 */
val screenCatalog: Map<String, @Composable () -> Unit> = mapOf(
    "NotesScreen_filled" to { NotesScreen(state = NotesPreviewData.filled, onEvent = {}) },
    "NotesScreen_empty" to { NotesScreen(state = NotesPreviewData.empty, onEvent = {}) },
    "NotesScreen_error" to { NotesScreen(state = NotesPreviewData.error, onEvent = {}) },
    "NotesScreen_loading" to { NotesScreen(state = NotesPreviewData.loading, onEvent = {}) },
    "AboutScreen_default" to { AboutScreen(state = AboutPreviewData.default, onBackClick = {}) }
)
