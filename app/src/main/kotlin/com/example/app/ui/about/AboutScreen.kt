package com.example.app.ui.about

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.app.R
import com.example.app.ui.theme.AppTheme
import org.koin.androidx.compose.koinViewModel

/** testTags de l'ecran : utilises par les tests ET par les dumps UI (tools/see.ps1). */
object AboutTags {
    const val BACK = "about_back"
    const val NAME = "about_app_name"
    const val VERSION = "about_version"
}

/** Point d'entree avec etat : recupere le ViewModel (Koin) et delegue a l'ecran sans etat. */
@Composable
fun AboutRoute(
    onBackClick: () -> Unit,
    viewModel: AboutViewModel = koinViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    AboutScreen(state = state, onBackClick = onBackClick)
}

/** Ecran SANS etat : tout vient de [state], tout sort par [onBackClick]. Testable et previewable. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(
    state: AboutUiState,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.about_title)) },
                navigationIcon = {
                    IconButton(onClick = onBackClick, modifier = Modifier.testTag(AboutTags.BACK)) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.about_back)
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.app_name),
                modifier = Modifier.testTag(AboutTags.NAME)
            )
            Text(
                text = stringResource(R.string.about_version, state.version),
                modifier = Modifier.padding(top = 8.dp).testTag(AboutTags.VERSION)
            )
        }
    }
}

/** Etats d'exemple partages par les @Preview et par ScreenCatalog (tests). */
object AboutPreviewData {
    val default = AboutUiState()
}

@Preview(showBackground = true)
@Composable
private fun AboutScreenPreview() {
    AppTheme { AboutScreen(state = AboutPreviewData.default, onBackClick = {}) }
}
