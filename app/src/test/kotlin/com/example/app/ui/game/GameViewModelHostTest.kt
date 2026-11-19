package com.example.app.ui.game

import com.example.app.data.link.InMemoryGameSession
import com.example.app.data.link.InMemoryLinkPair
import com.example.app.game.contract.Engine
import com.example.app.game.contract.GameState
import com.example.app.game.contract.Inputs
import com.example.app.game.contract.NetMessage
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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * CA2 (04-rendu) : role CHARACTER = hote de la simulation. Les [Inputs] passes
 * au moteur = [Inputs] de l'[com.example.app.game.contract.InputSource] (CHARACTER)
 * fusionnes avec le dernier [NetMessage.EnvInput] recu sur la liaison (rotation
 * du monde), et un [NetMessage.Snapshot] est envoye tous les 2 pas.
 */
class GameViewModelHostTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    /** Duree d'un pas en ns, derivee du GameConfig (comme le ViewModel). */
    private val stepNanos = (GameConfig.STEP_SECONDS * 1_000_000_000L).toLong()

    private class RecordingEngine : Engine {
        val recordedInputs = mutableListOf<Inputs>()

        override fun step(state: GameState, inputs: Inputs): GameState {
            recordedInputs += inputs
            return state.copy(tick = state.tick + 1)
        }
    }

    private lateinit var pair: InMemoryLinkPair
    private lateinit var engine: RecordingEngine
    private lateinit var session: InMemoryGameSession
    private lateinit var inputSource: FakeInputSource
    private lateinit var viewModel: GameViewModel

    @Before
    fun setUp() {
        pair = InMemoryLinkPair()
        engine = RecordingEngine()
        session = InMemoryGameSession()
        inputSource = FakeInputSource()
        viewModel = GameViewModel(engine, session, inputSource)
    }

    /**
     * Demarre la session CHARACTER, collecte les messages recus sur le bout B
     * de la liaison, execute [test], puis renvoie les messages recus.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    private suspend fun TestScope.startHostAndCollect(test: suspend () -> Unit): List<NetMessage> {
        keepCollecting(viewModel.uiState)
        val received = mutableListOf<NetMessage>()
        // dispatcher non confiné : le collecteur recoit tout de suite, sans attendre de suspension
        val collector: Job = launch(UnconfinedTestDispatcher(testScheduler)) {
            pair.b.incoming.collect { received += it }
        }
        session.start(Role.CHARACTER, pair.a)
        test()
        collector.cancel()
        return received
    }

    @Test
    fun `when host steps then character inputs are combined with latest env angle`() = runTest {
        val received = startHostAndCollect {
            inputSource.emit(Inputs(moveX = 1.0, jump = true))
            pair.b.send(NetMessage.EnvInput(seq = 1, angleDeg = 30.0, special = false))

            viewModel.onEvent(GameEvent.Frame(0L))
            viewModel.onEvent(GameEvent.Frame(stepNanos * 2L))
        }

        assertEquals(2, engine.recordedInputs.size)
        val last = engine.recordedInputs.last()
        assertEquals(1.0, last.moveX, 1e-9)
        assertTrue(last.jump)
        assertEquals(30.0, last.worldAngleDeg, 1e-9)
        // le snapshot envoye au tick 2 est couvert par les tests dedies aux snapshots
        assertEquals(1, received.count { it is NetMessage.Snapshot })
    }

    @Test
    fun `when no env input received then world angle stays zero`() = runTest {
        startHostAndCollect {
            inputSource.emit(Inputs(moveX = 0.5, jump = false))

            viewModel.onEvent(GameEvent.Frame(0L))
            viewModel.onEvent(GameEvent.Frame(stepNanos))
        }

        assertEquals(1, engine.recordedInputs.size)
        assertEquals(0.5, engine.recordedInputs.last().moveX, 1e-9)
        assertEquals(0.0, engine.recordedInputs.last().worldAngleDeg, 1e-9)
    }

    @Test
    fun `when several env inputs arrive then the most recent one wins`() = runTest {
        startHostAndCollect {
            pair.b.send(NetMessage.EnvInput(seq = 1, angleDeg = 30.0, special = false))
            pair.b.send(NetMessage.EnvInput(seq = 2, angleDeg = 90.0, special = false))

            viewModel.onEvent(GameEvent.Frame(0L))
            viewModel.onEvent(GameEvent.Frame(stepNanos))
        }

        assertEquals(1, engine.recordedInputs.size)
        assertEquals(90.0, engine.recordedInputs.last().worldAngleDeg, 1e-9)
    }

    @Test
    fun `when env input has special flag then engine gets dark world`() = runTest {
        startHostAndCollect {
            pair.b.send(NetMessage.EnvInput(seq = 1, angleDeg = 0.0, special = true))

            viewModel.onEvent(GameEvent.Frame(0L))
            viewModel.onEvent(GameEvent.Frame(stepNanos))
        }

        assertTrue(engine.recordedInputs.last().darkWorld)
    }

    @Test
    fun `when host steps three times then one snapshot carries tick two`() = runTest {
        val received = startHostAndCollect {
            viewModel.onEvent(GameEvent.Frame(0L))
            viewModel.onEvent(GameEvent.Frame(stepNanos * 3L))
        }

        val snapshots = received.filterIsInstance<NetMessage.Snapshot>()
        assertEquals(1, snapshots.size)
        assertEquals(2L, snapshots.single().state.tick)
    }

    @Test
    fun `when host steps six times then three snapshots carry ticks two four six`() = runTest {
        val received = startHostAndCollect {
            viewModel.onEvent(GameEvent.Frame(0L))
            viewModel.onEvent(GameEvent.Frame(stepNanos * 6L))
        }

        val snapshots = received.filterIsInstance<NetMessage.Snapshot>()
        assertEquals(listOf(2L, 4L, 6L), snapshots.map { it.state.tick })
    }
}
