package com.example.app.ui.game

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
}

/**
 * Point d'entree avec etat : recupere le ViewModel (Koin) et delegue a l'ecran sans etat.
 * C'est ICI UNIQUEMENT que vit la boucle d'images : chaque frame envoie un
 * [GameEvent.Frame] au ViewModel, qui fait avancer le moteur a pas fixe.
 */
@Composable
fun GameRoute(
    viewModel: GameViewModel = koinViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) {
        while (isActive) {
            withFrameNanos { timeNanos -> viewModel.onEvent(GameEvent.Frame(timeNanos)) }
        }
    }
    GameScreen(state = state)
}

/** Ecran SANS etat : tout vient de [state]. Testable et previewable. */
@Composable
fun GameScreen(
    state: GameUiState,
    modifier: Modifier = Modifier
) {
    val game = state.game
    val angle = state.liveWorldAngleDeg ?: game?.worldAngleDeg ?: 0.0
    Canvas(modifier = modifier.fillMaxSize().testTag(GameTags.CANVAS)) {
        if (game == null) return@Canvas
        drawGame(DEFAULT_LEVEL, game.player, angle)
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
}

@Preview(showBackground = true)
@Composable
private fun GameScreenPreview() {
    AppTheme { GameScreen(state = GamePreviewData.default) }
}

@Preview(showBackground = true)
@Composable
private fun GameScreenRotatedPreview() {
    AppTheme { GameScreen(state = GamePreviewData.rotated) }
}
