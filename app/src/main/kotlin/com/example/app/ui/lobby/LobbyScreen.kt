package com.example.app.ui.lobby

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.app.R
import com.example.app.game.contract.Role
import com.example.app.ui.theme.AppTheme
import org.koin.androidx.compose.koinViewModel

/** testTags de l'ecran : utilises par les tests ET par les dumps UI (tools/see.ps1). */
object LobbyTags {
    const val ROLE_CHARACTER = "lobby_role_character"
    const val ROLE_ENVIRONMENT = "lobby_role_environment"
    const val IP = "lobby_ip"
    const val PORT = "lobby_port"
    const val START = "lobby_start"
    const val SIMULATED = "lobby_simulated"
    const val ERROR = "lobby_error"
}

/** Point d'entree avec etat : recupere le ViewModel (Koin) et delegue a l'ecran sans etat. */
@Composable
fun LobbyRoute(
    viewModel: LobbyViewModel = koinViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    LobbyScreen(state = state, onEvent = viewModel::onEvent)
}

/** Ecran SANS etat : tout vient de [state], tout sort par [onEvent]. Testable et previewable. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LobbyScreen(
    state: LobbyUiState,
    onEvent: (LobbyEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        topBar = { TopAppBar(title = { Text(stringResource(R.string.lobby_title)) }) }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(text = stringResource(R.string.lobby_role_label))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = state.role == Role.CHARACTER,
                    onClick = { onEvent(LobbyEvent.RoleSelected(Role.CHARACTER)) },
                    label = { Text(stringResource(R.string.lobby_role_character)) },
                    modifier = Modifier.testTag(LobbyTags.ROLE_CHARACTER)
                )
                FilterChip(
                    selected = state.role == Role.ENVIRONMENT,
                    onClick = { onEvent(LobbyEvent.RoleSelected(Role.ENVIRONMENT)) },
                    label = { Text(stringResource(R.string.lobby_role_environment)) },
                    modifier = Modifier.testTag(LobbyTags.ROLE_ENVIRONMENT)
                )
            }
            OutlinedTextField(
                value = state.ip,
                onValueChange = { onEvent(LobbyEvent.IpChanged(it)) },
                label = { Text(stringResource(R.string.lobby_ip_label)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().testTag(LobbyTags.IP)
            )
            OutlinedTextField(
                value = state.port,
                onValueChange = { onEvent(LobbyEvent.PortChanged(it)) },
                label = { Text(stringResource(R.string.lobby_port_label)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().testTag(LobbyTags.PORT)
            )
            state.error?.let { error ->
                Text(text = error.message(), modifier = Modifier.testTag(LobbyTags.ERROR))
            }
            Button(
                onClick = { onEvent(LobbyEvent.StartClicked) },
                modifier = Modifier.fillMaxWidth().testTag(LobbyTags.START)
            ) {
                Text(stringResource(R.string.lobby_start))
            }
            OutlinedButton(
                onClick = { onEvent(LobbyEvent.SimulatedPartnerClicked) },
                modifier = Modifier.fillMaxWidth().testTag(LobbyTags.SIMULATED)
            ) {
                Text(stringResource(R.string.lobby_simulated))
            }
        }
    }
}

@Composable
private fun LobbyError.message(): String = when (this) {
    LobbyError.RoleNotChosen -> stringResource(R.string.lobby_error_role)
    LobbyError.InvalidPort -> stringResource(R.string.lobby_error_port)
}

/** Etats d'exemple partages par les @Preview et par ScreenCatalog (tests). */
object LobbyPreviewData {
    val default = LobbyUiState()
    val character = LobbyUiState(role = Role.CHARACTER, ip = "192.168.1.10", port = "4242")
    val error = LobbyUiState(error = LobbyError.RoleNotChosen)
}

@Preview(showBackground = true)
@Composable
private fun LobbyScreenPreview() {
    AppTheme { LobbyScreen(state = LobbyPreviewData.default, onEvent = {}) }
}

@Preview(showBackground = true)
@Composable
private fun LobbyScreenCharacterPreview() {
    AppTheme { LobbyScreen(state = LobbyPreviewData.character, onEvent = {}) }
}
