package com.example.app.ui.game

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
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

/** Nombre de tuiles par cote la plus courte du canvas (squelette ; geometrie reelle = feature 05). */
private const val LEVEL_TILES = 10

private val GROUND_COLOR = Color(0xFF555555)
private val PLAYER_COLOR = Color(0xFF0066CC)

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
    Canvas(modifier = modifier.fillMaxSize().testTag(GameTags.CANVAS)) {
        if (game == null) return@Canvas
        val tileSize = size.minDimension / LEVEL_TILES
        // Monde : rotation autour du centre du canvas (rendu complet = feature 04).
        rotate(degrees = game.worldAngleDeg.toFloat(), pivot = center) {
            // Sol : une ligne d'une tuile en bas du canvas (y vers le bas).
            drawRect(
                color = GROUND_COLOR,
                topLeft = Offset(0f, size.height - tileSize),
                size = Size(size.width, tileSize)
            )
            // Joueur : un carre d'une tuile en (player.x, player.y).
            drawRect(
                color = PLAYER_COLOR,
                topLeft = Offset((game.player.x * tileSize).toFloat(), (game.player.y * tileSize).toFloat()),
                size = Size(tileSize, tileSize)
            )
        }
    }
}

/** Etats d'exemple partages par les @Preview et par ScreenCatalog (tests). */
object GamePreviewData {
    val default = GameUiState(
        role = Role.CHARACTER,
        linkState = LinkState.CONNECTED,
        game = GameState(
            tick = 60L,
            player = PlayerState(x = 2.0, y = 8.0, vx = 0.0, vy = 0.0, onGround = true),
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
    AppTheme { GameScreen(state = GamePreviewData.default) }
}
