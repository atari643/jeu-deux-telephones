package com.example.app.ui.game

import com.example.app.data.link.InMemoryGameSession
import com.example.app.data.link.InMemoryLinkPair
import com.example.app.game.contract.Engine
import com.example.app.game.contract.GameState
import com.example.app.game.contract.GameStatus
import com.example.app.game.contract.Inputs
import com.example.app.game.contract.LinkState
import com.example.app.game.contract.NetMessage
import com.example.app.game.contract.PlayerState
import com.example.app.game.contract.Role
import com.example.app.game.engine.GameConfig
import com.example.app.testing.FakeInputSource
import com.example.app.testing.MainDispatcherRule
import com.example.app.testing.keepCollecting
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * Pattern : moteur de test qui compte les pas + session en mémoire + FakeInputSource
 * + keepCollecting(uiState) + onEvent(Frame) + assert sur uiState.value et sur les pas.
 */
class GameViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    /** Durée d'un pas en ns, dérivée du GameConfig (comme le ViewModel). */
    private val stepNanos = (GameConfig.STEP_SECONDS * 1_000_000_000L).toLong()

    private class RecordingEngine : Engine {
        val recordedInputs = mutableListOf<Inputs>()

        override fun step(state: GameState, inputs: Inputs): GameState {
            recordedInputs += inputs
            return state.copy(tick = state.tick + 1)
        }
    }

    private lateinit var engine: RecordingEngine
    private lateinit var session: InMemoryGameSession
    private lateinit var inputSource: FakeInputSource
    private lateinit var viewModel: GameViewModel

    @Before
    fun setUp() {
        engine = RecordingEngine()
        session = InMemoryGameSession()
        inputSource = FakeInputSource()
        viewModel = GameViewModel(engine, session, inputSource)
    }

    @Test
    fun `when created then state has no session and no game`() = runTest {
        keepCollecting(viewModel.uiState)

        val state = viewModel.uiState.value
        assertNull(state.role)
        assertNull(state.linkState)
        assertNull(state.game)
        assertEquals(GameError.NoSession, state.error)
    }

    @Test
    fun `when frames arrive without session then engine is not stepped`() = runTest {
        keepCollecting(viewModel.uiState)

        viewModel.onEvent(GameEvent.Frame(0L))
        viewModel.onEvent(GameEvent.Frame(stepNanos * 3))

        assertEquals(0, engine.recordedInputs.size)
        assertNull(viewModel.uiState.value.game)
    }

    @Test
    fun `when session started then game starts with role and link state`() = runTest {
        keepCollecting(viewModel.uiState)

        session.start(Role.CHARACTER, InMemoryLinkPair().a)

        val state = viewModel.uiState.value
        assertEquals(Role.CHARACTER, state.role)
        assertEquals(LinkState.CONNECTED, state.linkState)
        assertEquals(0L, state.game?.tick)
        assertNull(state.error)
    }

    @Test
    fun `when frames arrive then engine is stepped with fixed steps`() = runTest {
        keepCollecting(viewModel.uiState)
        session.start(Role.CHARACTER, InMemoryLinkPair().a)

        viewModel.onEvent(GameEvent.Frame(0L))
        viewModel.onEvent(GameEvent.Frame(stepNanos * 3))

        assertEquals(3, engine.recordedInputs.size)
        assertEquals(3L, viewModel.uiState.value.game?.tick)
    }

    @Test
    fun `when inputs are emitted then engine steps with those inputs`() = runTest {
        keepCollecting(viewModel.uiState)
        session.start(Role.CHARACTER, InMemoryLinkPair().a)
        inputSource.emit(Inputs(moveX = 0.5, jump = true))

        viewModel.onEvent(GameEvent.Frame(0L))
        viewModel.onEvent(GameEvent.Frame(stepNanos))

        assertEquals(1, engine.recordedInputs.size)
        assertEquals(0.5, engine.recordedInputs.last().moveX, 1e-9)
        assertEquals(true, engine.recordedInputs.last().jump)
    }

    @Test
    fun `when session ends then game is stopped`() = runTest {
        keepCollecting(viewModel.uiState)
        session.start(Role.CHARACTER, InMemoryLinkPair().a)

        session.end()

        val state = viewModel.uiState.value
        assertNull(state.role)
        assertNull(state.linkState)
        assertNull(state.game)
        assertEquals(GameError.NoSession, state.error)
    }

    /** CA5 : moteur qui met la partie en [status] apres un seul pas. */
    private class TerminalEngine(private val status: GameStatus = GameStatus.WON) : Engine {
        val recordedStates = mutableListOf<GameState>()
        val recordedInputs = mutableListOf<Inputs>()

        override fun step(state: GameState, inputs: Inputs): GameState {
            recordedStates += state
            recordedInputs += inputs
            return state.copy(tick = state.tick + 1, status = status)
        }
    }

    @Test
    fun `when game is won then a final snapshot is sent and engine is not stepped anymore`() = runTest {
        val pair = InMemoryLinkPair()
        val terminalEngine = TerminalEngine(GameStatus.WON)
        viewModel = GameViewModel(terminalEngine, session, inputSource)
        keepCollecting(viewModel.uiState)
        val received = mutableListOf<NetMessage>()
        val collector = launch(UnconfinedTestDispatcher(testScheduler)) {
            pair.b.incoming.collect { received += it }
        }
        session.start(Role.CHARACTER, pair.a)

        try {
            viewModel.onEvent(GameEvent.Frame(0L))
            viewModel.onEvent(GameEvent.Frame(stepNanos * 3L))

            assertEquals(GameStatus.WON, viewModel.uiState.value.game?.status)
            val snapshots = received.filterIsInstance<NetMessage.Snapshot>()
            assertEquals(1, snapshots.size)
            assertEquals(GameStatus.WON, snapshots.single().state.status)

            viewModel.onEvent(GameEvent.Frame(stepNanos * 6L))
            assertEquals(1, terminalEngine.recordedInputs.size)
        } finally {
            collector.cancel()
        }
    }

    @Test
    fun `when replay pressed after game over then a new game starts and engine steps again`() = runTest {
        val terminalEngine = TerminalEngine(GameStatus.WON)
        viewModel = GameViewModel(terminalEngine, session, inputSource)
        keepCollecting(viewModel.uiState)
        session.start(Role.CHARACTER, InMemoryLinkPair().a)

        viewModel.onEvent(GameEvent.Frame(0L))
        viewModel.onEvent(GameEvent.Frame(stepNanos))
        assertEquals(GameStatus.WON, viewModel.uiState.value.game?.status)

        viewModel.onEvent(GameEvent.Replay)
        assertEquals(GameStatus.RUNNING, viewModel.uiState.value.game?.status)
        assertEquals(0L, viewModel.uiState.value.game?.tick)

        viewModel.onEvent(GameEvent.Frame(0L))
        viewModel.onEvent(GameEvent.Frame(stepNanos))
        assertEquals(2, terminalEngine.recordedInputs.size)
        assertEquals(0L, terminalEngine.recordedStates.last().tick)
    }

    @Test
    fun `when link disconnected then engine is not stepped anymore`() = runTest {
        val pair = InMemoryLinkPair()
        keepCollecting(viewModel.uiState)
        session.start(Role.CHARACTER, pair.a)

        viewModel.onEvent(GameEvent.Frame(0L))
        viewModel.onEvent(GameEvent.Frame(stepNanos))
        assertEquals(1L, viewModel.uiState.value.game?.tick)

        pair.a.close()

        viewModel.onEvent(GameEvent.Frame(stepNanos * 2L))
        assertEquals(LinkState.DISCONNECTED, viewModel.uiState.value.linkState)
        assertEquals(1, engine.recordedInputs.size)
    }

    @Test
    fun `when replay pressed as environment then displayed game is cleared`() = runTest {
        val pair = InMemoryLinkPair()
        keepCollecting(viewModel.uiState)
        session.start(Role.ENVIRONMENT, pair.b)
        pair.a.send(
            NetMessage.Snapshot(
                GameState(
                    tick = 5L,
                    player = PlayerState(x = 1.0, y = 1.0, vx = 0.0, vy = 0.0, onGround = true),
                    worldAngleDeg = 0.0,
                    levelId = GameConfig.START_LEVEL_ID,
                    status = GameStatus.RUNNING
                )
            )
        )
        assertEquals(5L, viewModel.uiState.value.game?.tick)

        viewModel.onEvent(GameEvent.Replay)

        assertNull(viewModel.uiState.value.game)
    }
}
