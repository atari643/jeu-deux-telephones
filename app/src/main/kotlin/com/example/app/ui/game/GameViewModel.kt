package com.example.app.ui.game

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.app.game.contract.Engine
import com.example.app.game.contract.GameLink
import com.example.app.game.contract.GameSession
import com.example.app.game.contract.GameState
import com.example.app.game.contract.GameStatus
import com.example.app.game.contract.InputSource
import com.example.app.game.contract.Inputs
import com.example.app.game.contract.LinkState
import com.example.app.game.contract.NetMessage
import com.example.app.game.contract.PlayerState
import com.example.app.game.contract.Role
import com.example.app.game.engine.GameConfig
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val STOP_TIMEOUT_MS = 5_000L

/**
 * ViewModel de l'ecran de jeu (squelette de la feature 00-contrat, CA6).
 * Lit la [GameSession] (remplie par le lobby de B) et fait avancer le
 * [Engine] a pas fixe [GameConfig.STEP_SECONDS], un pas par tranche de
 * temps reel accumulee via [GameEvent.Frame].
 *
 * Role CHARACTER (hote, 04-rendu CA2) : seul role qui simule. Les [Inputs]
 * de chaque pas = ceux de l'[InputSource] fusionnes avec le dernier
 * [NetMessage.EnvInput] recu ; un [NetMessage.Snapshot] est envoye a
 * l'autre telephone tous les [GameConfig.SNAPSHOT_EVERY_N_STEPS] pas.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class GameViewModel(private val engine: Engine, session: GameSession, private val inputSource: InputSource) : ViewModel() {

    private val role: StateFlow<Role?> =
        session.role.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), null)

    private val link: StateFlow<GameLink?> =
        session.link.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), null)

    private val linkState: StateFlow<LinkState?> =
        link.flatMapLatest { currentLink -> currentLink?.state ?: flowOf(null) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), null)

    private val game = MutableStateFlow<GameState?>(null)
    private val charInput = MutableStateFlow(Inputs())
    private val lastEnvInput = MutableStateFlow<NetMessage.EnvInput?>(null)
    private val stepNanos: Long = (GameConfig.STEP_SECONDS * 1_000_000_000L).toLong()

    private var inputJob: Job? = null
    private var incomingJob: Job? = null
    private var lastFrameNanos: Long? = null
    private var accumulatorNanos = 0L

    val uiState: StateFlow<GameUiState> =
        combine(role, linkState, game) { currentRole, currentLinkState, currentGame ->
            GameUiState(
                role = currentRole,
                linkState = currentLinkState,
                game = currentGame,
                error = if (currentRole == null) GameError.NoSession else null
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), GameUiState(error = GameError.NoSession))

    init {
        viewModelScope.launch {
            role.collect { currentRole ->
                inputJob?.cancel()
                incomingJob?.cancel()
                game.value = null
                charInput.value = Inputs()
                lastEnvInput.value = null
                lastFrameNanos = null
                accumulatorNanos = 0L
                if (currentRole != null) {
                    game.value = initialGame()
                    inputJob = launch { inputSource.inputs(currentRole).collect { charInput.value = it } }
                    incomingJob = launch {
                        link.flatMapLatest { currentLink -> currentLink?.incoming ?: flowOf() }
                            .collect { message ->
                                when (message) {
                                    // CA2 : on garde le dernier angle recu (le plus recent l'emporte).
                                    is NetMessage.EnvInput -> lastEnvInput.value = message

                                    // Les autres messages (Snapshot entrants, Hello, Ready, Ping...)
                                    // sont gerés par les autres tâches de la feature 04.
                                    else -> Unit
                                }
                            }
                    }
                }
            }
        }
    }

    fun onEvent(event: GameEvent) {
        when (event) {
            is GameEvent.Frame -> stepFrame(event.timeNanos)

            // Fin de partie (WON/LOST) : gestion dans la tâche 6 de la feature 04.
            GameEvent.Replay -> Unit
        }
    }

    /** Accumule le temps reel et appelle [Engine.step] un nombre entier de fois (pas fixe). */
    private fun stepFrame(timeNanos: Long) {
        if (game.value == null) return
        if (role.value != Role.CHARACTER) return
        val previous = lastFrameNanos ?: timeNanos
        lastFrameNanos = timeNanos
        accumulatorNanos += (timeNanos - previous).coerceAtLeast(0L)
        while (accumulatorNanos >= stepNanos) {
            accumulatorNanos -= stepNanos
            game.update { current -> if (current != null) engine.step(current, buildStepInputs()) else current }
            sendSnapshotIfDue(game.value)
        }
    }

    /**
     * CA2 : fusion des [Inputs] du personnage (de l'[InputSource]) avec le
     * dernier [NetMessage.EnvInput] recu (rotation du monde, monde sombre).
     */
    private fun buildStepInputs(): Inputs {
        val character = charInput.value
        val env = lastEnvInput.value
        return Inputs(
            moveX = character.moveX,
            jump = character.jump,
            worldAngleDeg = env?.angleDeg ?: 0.0,
            darkWorld = env?.special ?: false
        )
    }

    /** CA2 : l'hote envoie un [NetMessage.Snapshot] tous les [GameConfig.SNAPSHOT_EVERY_N_STEPS] pas. */
    private fun sendSnapshotIfDue(newState: GameState?) {
        val currentLink = link.value
        if (newState != null && currentLink != null && newState.tick % GameConfig.SNAPSHOT_EVERY_N_STEPS == 0L) {
            viewModelScope.launch { currentLink.send(NetMessage.Snapshot(newState)) }
        }
    }

    private fun initialGame() = GameState(
        tick = 0L,
        player = PlayerState(x = 0.0, y = 0.0, vx = 0.0, vy = 0.0, onGround = true),
        worldAngleDeg = 0.0,
        levelId = GameConfig.START_LEVEL_ID,
        status = GameStatus.RUNNING
    )
}
