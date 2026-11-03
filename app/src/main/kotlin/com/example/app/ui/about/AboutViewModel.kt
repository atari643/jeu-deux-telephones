package com.example.app.ui.about

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Version affichee sur l'ecran About (build.gradle intouchable, pas de BuildConfig). */
const val APP_VERSION = "1.0"

class AboutViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(AboutUiState(version = APP_VERSION))

    val uiState: StateFlow<AboutUiState> = _uiState.asStateFlow()
}
