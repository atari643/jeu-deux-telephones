package com.example.app.ui.game

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.app.R
import com.example.app.game.contract.GameState
import com.example.app.game.contract.GameStatus
import com.example.app.game.contract.LinkState
import com.example.app.game.contract.PlayerState
import com.example.app.game.contract.Role
import com.example.app.game.engine.GameConfig
import com.example.app.ui.theme.AppTheme
import kotlinx.coroutines.isActive
import org.koin.androidx.compose.koinViewModel

/** testTags de l'ecran : utilises par les tests ET par les dumps UI (tools/see.ps1). */
object GameTags {
    const val CANVAS = "game_canvas"
    const val RESULT = "game_result"
    const val REPLAY = "game_replay"
    const val DISCONNECTED = "game_disconnected"
    const val EXIT = "game_exit"
}

/**
 * Point d'entree avec etat : recupere le ViewModel (Koin) et delegue a l'ecran sans etat.
 * C'est ICI UNIQUEMENT que vit la boucle d'images : chaque frame envoie un
 * [GameEvent.Frame] au ViewModel, qui fait avancer le moteur a pas fixe.
 * [onExit] retourne au lobby (CA5 : liaison decollee).
 */
@Composable
fun GameRoute(
    onExit: () -> Unit = {},
    viewModel: GameViewModel = koinViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) {
        while (isActive) {
            withFrameNanos { timeNanos -> viewModel.onEvent(GameEvent.Frame(timeNanos)) }
        }
    }
    GameScreen(state = state, onEvent = viewModel::onEvent, onExit = onExit)
}

/** Ecran SANS etat : tout vient de [state], tout sort par [onEvent]/[onExit]. */
@Composable
fun GameScreen(
    state: GameUiState,
    onEvent: (GameEvent) -> Unit,
    onExit: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val game = state.game
    val angle = state.liveWorldAngleDeg ?: game?.worldAngleDeg ?: 0.0
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize().testTag(GameTags.CANVAS)) {
            if (game == null) return@Canvas
            drawGame(DEFAULT_LEVEL, game.player, angle)
        }
        when {
            // CA5 : la decollee prime : message + retour au lobby.
            state.linkState == LinkState.DISCONNECTED -> DisconnectedPanel(onExit)

            // CA5 : panneau de fin de partie avec bouton « Rejouer ».
            game?.status == GameStatus.WON -> ResultPanel(
                resultText = stringResource(R.string.game_won),
                onReplay = { onEvent(GameEvent.Replay) }
            )

            game?.status == GameStatus.LOST -> ResultPanel(
                resultText = stringResource(R.string.game_lost),
                onReplay = { onEvent(GameEvent.Replay) }
            )

            else -> Unit
        }
    }
}

/** Panneau de fin de partie (WON/LOST) : texte du resultat + bouton « Rejouer » (CA5). */
@Composable
private fun ResultPanel(resultText: String, onReplay: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = resultText,
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.testTag(GameTags.RESULT)
        )
        Button(onClick = onReplay, modifier = Modifier.testTag(GameTags.REPLAY)) {
            Text(stringResource(R.string.game_replay))
        }
    }
}

/** Panneau de decollee : message + bouton retour au lobby (CA5). */
@Composable
private fun DisconnectedPanel(onExit: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = stringResource(R.string.game_disconnected),
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.testTag(GameTags.DISCONNECTED)
        )
        Button(onClick = onExit, modifier = Modifier.testTag(GameTags.EXIT)) {
            Text(stringResource(R.string.game_back_to_lobby))
        }
    }
}

/**
 * Etats d'exemple partages par les @Preview et par ScreenCatalog (tests).
 * Le joueur est pose sur le sol de [DEFAULT_LEVEL] (pieds en y = 2.0, CA4).
 */
object GamePreviewData {
    val default = GameUiState(
        role = Role.CHARACTER,
        linkState = LinkState.CONNECTED,
        game = GameState(
            tick = 60L,
            player = PlayerState(x = 4.5, y = 2.0, vx = 0.0, vy = 0.0, onGround = true),
            worldAngleDeg = 0.0,
            levelId = GameConfig.START_LEVEL_ID,
            status = GameStatus.RUNNING
        ),
        error = null
    )

    /** Monde tourne de 90 deg : snapshot Roborazzi GameScreen_rotated (CA4). */
    val rotated = GameUiState(
        role = Role.CHARACTER,
        linkState = LinkState.CONNECTED,
        game = GameState(
            tick = 60L,
            player = PlayerState(x = 4.5, y = 2.0, vx = 0.0, vy = 0.0, onGround = true),
            worldAngleDeg = 90.0,
            levelId = GameConfig.START_LEVEL_ID,
            status = GameStatus.RUNNING
        ),
        error = null
    )

    /** Partie gagnee : panneau de fin avec « Rejouer » (CA5). */
    val won = GameUiState(
        role = Role.CHARACTER,
        linkState = LinkState.CONNECTED,
        game = GameState(
            tick = 120L,
            player = PlayerState(x = 9.0, y = 3.0, vx = 0.0, vy = 0.0, onGround = true),
            worldAngleDeg = 0.0,
            levelId = GameConfig.START_LEVEL_ID,
            status = GameStatus.WON
        ),
        error = null
    )

    /** Partie perdue : panneau de fin avec « Rejouer » (CA5). */
    val lost = GameUiState(
        role = Role.CHARACTER,
        linkState = LinkState.CONNECTED,
        game = GameState(
            tick = 90L,
            player = PlayerState(x = 4.5, y = 4.5, vx = 0.0, vy = 1.0, onGround = false),
            worldAngleDeg = 0.0,
            levelId = GameConfig.START_LEVEL_ID,
            status = GameStatus.LOST
        ),
        error = null
    )

    /** Liaison decollee : message + retour au lobby (CA5). */
    val disconnected = GameUiState(
        role = Role.CHARACTER,
        linkState = LinkState.DISCONNECTED,
        game = GameState(
            tick = 60L,
            player = PlayerState(x = 4.5, y = 2.0, vx = 0.0, vy = 0.0, onGround = true),
            worldAngleDeg = 0.0,
            levelId = GameConfig.START_LEVEL_ID,
            status = GameStatus.RUNNING
        ),
        error = null
    )
}

@Preview(showBackground = true)
@Composable
private fun GameScreenPreview() {
    AppTheme { GameScreen(state = GamePreviewData.default, onEvent = {}) }
}

@Preview(showBackground = true)
@Composable
private fun GameScreenRotatedPreview() {
    AppTheme { GameScreen(state = GamePreviewData.rotated, onEvent = {}) }
}

@Preview(showBackground = true)
@Composable
private fun GameScreenWonPreview() {
    AppTheme { GameScreen(state = GamePreviewData.won, onEvent = {}) }
}

@Preview(showBackground = true)
@Composable
private fun GameScreenDisconnectedPreview() {
    AppTheme { GameScreen(state = GamePreviewData.disconnected, onEvent = {}, onExit = {}) }
}
