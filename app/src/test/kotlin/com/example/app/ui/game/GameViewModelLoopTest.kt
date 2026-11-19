package com.example.app.ui.game

import com.example.app.data.link.InMemoryGameSession
import com.example.app.data.link.InMemoryLinkPair
import com.example.app.game.contract.Engine
import com.example.app.game.contract.GameState
import com.example.app.game.contract.Inputs
import com.example.app.game.contract.Role
import com.example.app.game.engine.GameConfig
import com.example.app.testing.FakeInputSource
import com.example.app.testing.MainDispatcherRule
import com.example.app.testing.keepCollecting
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * CA1 (04-rendu) : sur [GameEvent.Frame], le [GameViewModel] appelle
 * [Engine.step] exactement `floor(temps ecoule / STEP)` fois, pas fixe
 * accumule entre deux images. Le moteur de test ne fait que compter.
 */
class GameViewModelLoopTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    /** Duree d'un pas en ns, derivee du GameConfig (comme le ViewModel). */
    private val stepNanos = (GameConfig.STEP_SECONDS * 1_000_000_000L).toLong()

    private class CountingEngine : Engine {
        var steps = 0
            private set

        override fun step(state: GameState, inputs: Inputs): GameState {
            steps++
            return state.copy(tick = state.tick + 1)
        }
    }

    private lateinit var engine: CountingEngine
    private lateinit var session: InMemoryGameSession
    private lateinit var viewModel: GameViewModel

    @Before
    fun setUp() {
        engine = CountingEngine()
        session = InMemoryGameSession()
        viewModel = GameViewModel(engine, session, FakeInputSource())
    }

    private fun startGame() {
        session.start(Role.CHARACTER, InMemoryLinkPair().a)
    }

    @Test
    fun `when first frame arrives then no step is executed`() = runTest {
        keepCollecting(viewModel.uiState)
        startGame()

        viewModel.onEvent(GameEvent.Frame(0L))

        assertEquals(0, engine.steps)
        assertEquals(0L, viewModel.uiState.value.game?.tick)
    }

    @Test
    fun `when elapsed time spans exactly k steps then engine steps k times`() = runTest {
        keepCollecting(viewModel.uiState)
        startGame()

        viewModel.onEvent(GameEvent.Frame(0L))
        viewModel.onEvent(GameEvent.Frame(stepNanos * 5L))

        assertEquals(5, engine.steps)
        assertEquals(5L, viewModel.uiState.value.game?.tick)
    }

    @Test
    fun `when elapsed time has a remainder then engine steps floor of it`() = runTest {
        keepCollecting(viewModel.uiState)
        startGame()

        viewModel.onEvent(GameEvent.Frame(0L))
        viewModel.onEvent(GameEvent.Frame(stepNanos * 2L + 1L))

        assertEquals(2, engine.steps)
        assertEquals(2L, viewModel.uiState.value.game?.tick)
    }

    @Test
    fun `when frames are shorter than a step then remainders accumulate`() = runTest {
        keepCollecting(viewModel.uiState)
        startGame()
        val halfStep = stepNanos / 2L

        viewModel.onEvent(GameEvent.Frame(0L))
        viewModel.onEvent(GameEvent.Frame(halfStep))
        assertEquals(0, engine.steps)

        viewModel.onEvent(GameEvent.Frame(halfStep + halfStep))

        assertEquals(1, engine.steps)
        assertEquals(1L, viewModel.uiState.value.game?.tick)
    }

    @Test
    fun `when a big gap arrives then only whole steps are executed`() = runTest {
        keepCollecting(viewModel.uiState)
        startGame()

        viewModel.onEvent(GameEvent.Frame(0L))
        viewModel.onEvent(GameEvent.Frame(stepNanos))
        viewModel.onEvent(GameEvent.Frame(stepNanos + stepNanos * 5L / 2L))

        // temps ecoule total = 3,5 pas -> floor = 3
        assertEquals(3, engine.steps)
        assertEquals(3L, viewModel.uiState.value.game?.tick)
    }

    @Test
    fun `when a frame goes back in time then no extra step is executed`() = runTest {
        keepCollecting(viewModel.uiState)
        startGame()

        viewModel.onEvent(GameEvent.Frame(0L))
        viewModel.onEvent(GameEvent.Frame(stepNanos * 2L))
        viewModel.onEvent(GameEvent.Frame(stepNanos))

        assertEquals(2, engine.steps)
    }

    @Test
    fun `when frames arrive without session then engine never steps`() = runTest {
        keepCollecting(viewModel.uiState)

        viewModel.onEvent(GameEvent.Frame(0L))
        viewModel.onEvent(GameEvent.Frame(stepNanos * 10L))

        assertEquals(0, engine.steps)
        assertNull(viewModel.uiState.value.game)
    }

    @Test
    fun `when session restarts then step counting starts over`() = runTest {
        keepCollecting(viewModel.uiState)
        startGame()

        viewModel.onEvent(GameEvent.Frame(0L))
        viewModel.onEvent(GameEvent.Frame(stepNanos * 3L))
        assertEquals(3, engine.steps)

        session.end()
        startGame()
        viewModel.onEvent(GameEvent.Frame(0L))
        viewModel.onEvent(GameEvent.Frame(stepNanos))

        assertEquals(4, engine.steps)
        assertEquals(1L, viewModel.uiState.value.game?.tick)
    }
}
