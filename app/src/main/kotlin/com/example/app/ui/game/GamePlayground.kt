package com.example.app.ui.game

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.app.R
import com.example.app.game.contract.Engine
import com.example.app.game.contract.GameState
import com.example.app.game.contract.GameStatus
import com.example.app.game.contract.Inputs
import com.example.app.game.contract.PlayerState
import com.example.app.game.engine.GameConfig
import com.example.app.game.engine.GameEngine
import com.example.app.game.engine.Level
import com.example.app.ui.theme.AppTheme
import kotlinx.coroutines.isActive

/** Constantes du simulateur PC (feature 04-rendu CA6). */
object PlaygroundConfig {

    /** Duree d'un pas fixe en nanos, identique au [GameConfig.STEP_SECONDS] du moteur. */
    val STEP_NANOS: Long = (GameConfig.STEP_SECONDS * 1_000_000_000L).toLong()

    /** Rotation du monde (degres) par clic sur les boutons de rotation. */
    const val ROTATION_STEP_DEG: Double = 15.0

    /** Abcisse du joueur : au centre de la tuile de depart (en tuiles). */
    const val START_X_OFFSET_TILES: Double = 0.5

    /** Ordonnee du joueur : les pieds sur la face haute de la rangee sous le depart (en tuiles). */
    const val START_FOOT_OFFSET_TILES: Double = 1.0
}

/**
 * Simulateur PC (04-rendu CA6) : fait tourner le VRAI [GameEngine] a pas fixe
 * sur le niveau de test [DEFAULT_LEVEL], sans telephone ni emulateur.
 *
 * Toute la logique (etat, pas fixe, inputs) est ici, en pur Kotlin, testee
 * par [GamePlaygroundTest] ; le composable [GamePlaygroundPreview] ne fait
 * qu'afficher l'etat et transmettre les clics. Les boutons remplacent les
 * capteurs : gauche/droite/saut basculent les [Inputs] du pas suivant, les
 * deux boutons de rotation font tourner le monde de
 * [PlaygroundConfig.ROTATION_STEP_DEG] degres par clic.
 */
class GamePlayground(private val engine: Engine, val level: Level) {

    private var currentState: GameState = initialState()
    private var moveHeld = 0
    private var jumpHeld = false
    private var angleDeg = 0.0
    private var lastFrameNanos: Long? = null
    private var accumulatorNanos = 0L

    /** Etat du moteur a afficher. */
    val state: GameState
        get() = currentState

    /** Rotation du monde courante (degres) : la gravite du moteur la suit. */
    val worldAngleDeg: Double
        get() = angleDeg

    /**
     * Pas fixe (CA6) : accumule le temps reel et consomme des tranches de
     * [PlaygroundConfig.STEP_NANOS] ; le [Engine] avance exactement
     * `floor(temps ecoule / pas)` fois par image. Plus aucun pas une fois
     * la partie terminee (WON/LOST).
     */
    fun onFrame(timeNanos: Long) {
        if (currentState.status != GameStatus.RUNNING) return
        val previous = lastFrameNanos ?: timeNanos
        lastFrameNanos = timeNanos
        accumulatorNanos += (timeNanos - previous).coerceAtLeast(0L)
        while (accumulatorNanos >= PlaygroundConfig.STEP_NANOS && currentState.status == GameStatus.RUNNING) {
            accumulatorNanos -= PlaygroundConfig.STEP_NANOS
            currentState = engine.step(currentState, inputs())
        }
    }

    /** Bouton gauche : cours a gauche (un second clic arrete la course). */
    fun left() {
        moveHeld = if (moveHeld == -1) 0 else -1
    }

    /** Bouton droite : cours a droite (un second clic arrete la course). */
    fun right() {
        moveHeld = if (moveHeld == 1) 0 else 1
    }

    /** Bouton saut : bascule le saut pour les pas suivants. */
    fun jump() {
        jumpHeld = !jumpHeld
    }

    /** Bouton rotation gauche : le monde tourne de [PlaygroundConfig.ROTATION_STEP_DEG] degres vers la gauche. */
    fun rotateLeft() {
        angleDeg -= PlaygroundConfig.ROTATION_STEP_DEG
    }

    /** Bouton rotation droite : le monde tourne de [PlaygroundConfig.ROTATION_STEP_DEG] degres vers la droite. */
    fun rotateRight() {
        angleDeg += PlaygroundConfig.ROTATION_STEP_DEG
    }

    /** [Inputs] du pas suivant, construits a partir des boutons actifs. */
    fun inputs(): Inputs =
        Inputs(
            moveX = moveHeld.toDouble(),
            jump = jumpHeld,
            worldAngleDeg = angleDeg
        )

    private fun initialState(): GameState =
        GameState(
            tick = 0L,
            player =
                PlayerState(
                    x = level.start.x + PlaygroundConfig.START_X_OFFSET_TILES,
                    y = level.start.y + PlaygroundConfig.START_FOOT_OFFSET_TILES,
                    vx = 0.0,
                    vy = 0.0,
                    onGround = true
                ),
            worldAngleDeg = 0.0,
            levelId = GameConfig.START_LEVEL_ID,
            status = GameStatus.RUNNING
        )
}

/** testTags du simulateur (boutons + canvas). */
object GamePlaygroundTags {
    const val CANVAS = "playground_canvas"
    const val LEFT = "playground_left"
    const val RIGHT = "playground_right"
    const val JUMP = "playground_jump"
    const val ROTATE_LEFT = "playground_rotate_left"
    const val ROTATE_RIGHT = "playground_rotate_right"
}

/**
 * Apercu interactif du simulateur PC (CA6) : jouable dans le mode interactif
 * des apercus Compose d'Android Studio. Dessine avec le MEME code que
 * [GameScreen] ([drawGame]) et les 5 boutons remplacent les capteurs.
 * Le composable n'affiche que [GamePlayground.state] et transmet les clics.
 */
@Preview(showBackground = true)
@Composable
fun GamePlaygroundPreview() {
    AppTheme {
        val playground = remember { GamePlayground(GameEngine(DEFAULT_LEVEL), DEFAULT_LEVEL) }
        var drawnState by remember { mutableStateOf(playground.state) }
        LaunchedEffect(Unit) {
            while (isActive) {
                withFrameNanos { timeNanos ->
                    playground.onFrame(timeNanos)
                    drawnState = playground.state
                }
            }
        }
        Box(modifier = Modifier.fillMaxSize()) {
            Canvas(modifier = Modifier.fillMaxSize().testTag(GamePlaygroundTags.CANVAS)) {
                drawGame(playground.level, drawnState.player, playground.worldAngleDeg)
            }
            Row(
                modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(onClick = playground::left, modifier = Modifier.testTag(GamePlaygroundTags.LEFT)) {
                    Text(stringResource(R.string.game_playground_left))
                }
                Button(onClick = playground::right, modifier = Modifier.testTag(GamePlaygroundTags.RIGHT)) {
                    Text(stringResource(R.string.game_playground_right))
                }
                Button(onClick = playground::jump, modifier = Modifier.testTag(GamePlaygroundTags.JUMP)) {
                    Text(stringResource(R.string.game_playground_jump))
                }
                Button(
                    onClick = playground::rotateLeft,
                    modifier = Modifier.testTag(GamePlaygroundTags.ROTATE_LEFT)
                ) {
                    Text(stringResource(R.string.game_playground_rotate_left))
                }
                Button(
                    onClick = playground::rotateRight,
                    modifier = Modifier.testTag(GamePlaygroundTags.ROTATE_RIGHT)
                ) {
                    Text(stringResource(R.string.game_playground_rotate_right))
                }
            }
        }
    }
}
