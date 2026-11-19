package com.example.app.ui.game

import com.example.app.data.link.InMemoryGameSession
import com.example.app.data.link.InMemoryLinkPair
import com.example.app.game.contract.Engine
import com.example.app.game.contract.GameState
import com.example.app.game.contract.GameStatus
import com.example.app.game.contract.Inputs
import com.example.app.game.contract.NetMessage
import com.example.app.game.contract.PlayerState
import com.example.app.game.contract.Role
import com.example.app.game.engine.GameConfig
import com.example.app.testing.FakeInputSource
import com.example.app.testing.MainDispatcherRule
import com.example.app.testing.keepCollecting
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * CA3 (04-rendu) : role ENVIRONMENT. L'etat affiche = dernier [NetMessage.Snapshot]
 * recu (le moteur ne simule PAS ici) ; le ViewModel envoie des [NetMessage.EnvInput]
 * (~30/s, un tous les [GameConfig.ENV_INPUT_EVERY_N_STEPS] pas) a partir de
 * [com.example.app.game.contract.InputSource].inputs(ENVIRONMENT), et sa propre
 * rotation est affichee tout de suite (liveWorldAngleDeg) sans attendre le snapshot.
 */
class GameViewModelEnvTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    /** Duree d'un pas en ns, derivee du GameConfig (comme le ViewModel). */
    private val stepNanos = (GameConfig.STEP_SECONDS * 1_000_000_000L).toLong()

    private class CountingEngine : Engine {
        var stepCount = 0

        override fun step(state: GameState, inputs: Inputs): GameState {
            stepCount += 1
            return state.copy(tick = state.tick + 1)
        }
    }

    private lateinit var pair: InMemoryLinkPair
    private lateinit var engine: CountingEngine
    private lateinit var session: InMemoryGameSession
    private lateinit var inputSource: FakeInputSource
    private lateinit var viewModel: GameViewModel

    @Before
    fun setUp() {
        pair = InMemoryLinkPair()
        engine = CountingEngine()
        session = InMemoryGameSession()
        inputSource = FakeInputSource()
        viewModel = GameViewModel(engine, session, inputSource)
    }

    /**
     * Demarre la session ENVIRONMENT, collecte les messages recus sur le bout A
     * de la liaison (celui de l'hote), execute [test], puis renvoie les messages recus.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    private suspend fun TestScope.startEnvAndCollect(test: suspend () -> Unit): List<NetMessage> {
        keepCollecting(viewModel.uiState)
        val received = mutableListOf<NetMessage>()
        // dispatcher non confiné : le collecteur recoit tout de suite, sans attendre de suspension
        val collector: Job = launch(UnconfinedTestDispatcher(testScheduler)) {
            pair.a.incoming.collect { received += it }
        }
        session.start(Role.ENVIRONMENT, pair.b)
        test()
        collector.cancel()
        return received
    }

    @Test
    fun `when env steps four times then two env inputs are sent with seq one and two`() = runTest {
        val received = startEnvAndCollect {
            inputSource.emit(Inputs(worldAngleDeg = 15.0, special = false))

            viewModel.onEvent(GameEvent.Frame(0L))
            viewModel.onEvent(GameEvent.Frame(stepNanos * 4L))
        }

        val envInputs = received.filterIsInstance<NetMessage.EnvInput>()
        assertEquals(listOf(1L, 2L), envInputs.map { it.seq })
        assertEquals(15.0, envInputs.last().angleDeg, 1e-9)
        // l'environnement ne simule pas : seul l'hote (CHARACTER) fait tourner le moteur
        assertTrue(received.all { it is NetMessage.EnvInput })
        assertEquals(0, engine.stepCount)
    }

    @Test
    fun `when env inputs change then the most recent reading is sent`() = runTest {
        val received = startEnvAndCollect {
            inputSource.emit(Inputs(worldAngleDeg = 10.0, special = false))
            inputSource.emit(Inputs(worldAngleDeg = 45.0, special = true))

            viewModel.onEvent(GameEvent.Frame(0L))
            viewModel.onEvent(GameEvent.Frame(stepNanos * 2L))
        }

        val last = received.filterIsInstance<NetMessage.EnvInput>().last()
        assertEquals(45.0, last.angleDeg, 1e-9)
        assertTrue(last.special)
    }

    @Test
    fun `when env receives a snapshot then uiState shows the snapshot state`() = runTest {
        val snapshot = snapshotState(tick = 7L)

        startEnvAndCollect {
            pair.a.send(NetMessage.Snapshot(snapshot))
        }

        assertEquals(snapshot, viewModel.uiState.value.game)
    }

    @Test
    fun `when env emits an angle then liveWorldAngleDeg updates without waiting the snapshot`() = runTest {
        startEnvAndCollect {
            inputSource.emit(Inputs(worldAngleDeg = 30.0, special = false))

            viewModel.onEvent(GameEvent.Frame(0L))
            viewModel.onEvent(GameEvent.Frame(stepNanos * 2L))
        }

        val state = viewModel.uiState.value
        // pas de snapshot recu : rien a afficher dans game, mais la rotation est live
        assertNull(state.game)
        assertTrue(state.liveWorldAngleDeg == 30.0)
    }
}

private fun snapshotState(tick: Long) = GameState(
    tick = tick,
    player = PlayerState(x = 3.0, y = 1.0, vx = 0.0, vy = 0.0, onGround = true),
    worldAngleDeg = 90.0,
    levelId = GameConfig.START_LEVEL_ID,
    status = GameStatus.RUNNING
)
