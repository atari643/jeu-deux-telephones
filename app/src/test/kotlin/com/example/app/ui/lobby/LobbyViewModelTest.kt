package com.example.app.ui.lobby

import com.example.app.data.link.InMemoryGameSession
import com.example.app.game.contract.LinkState
import com.example.app.game.contract.Role
import com.example.app.testing.FakeGameLinkFactory
import com.example.app.testing.MainDispatcherRule
import com.example.app.testing.keepCollecting
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNotSame
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * Pattern : fake factory + session en mémoire + keepCollecting(uiState) + onEvent(...)
 * + assert sur uiState.value et sur le GameSession remplit.
 */
class LobbyViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var factory: FakeGameLinkFactory
    private lateinit var session: InMemoryGameSession
    private lateinit var viewModel: LobbyViewModel

    @Before
    fun setUp() {
        factory = FakeGameLinkFactory()
        session = InMemoryGameSession()
        viewModel = LobbyViewModel(factory, session)
    }

    @Test
    fun `when created then state is empty`() = runTest {
        keepCollecting(viewModel.uiState)

        val state = viewModel.uiState.value
        assertNull(state.role)
        assertEquals("", state.ip)
        assertEquals("", state.port)
        assertNull(state.error)
    }

    @Test
    fun `when role selected then role is stored`() = runTest {
        keepCollecting(viewModel.uiState)

        viewModel.onEvent(LobbyEvent.RoleSelected(Role.ENVIRONMENT))

        assertEquals(Role.ENVIRONMENT, viewModel.uiState.value.role)
    }

    @Test
    fun `when ip and port changed then values are stored`() = runTest {
        keepCollecting(viewModel.uiState)

        viewModel.onEvent(LobbyEvent.IpChanged("192.168.1.10"))
        viewModel.onEvent(LobbyEvent.PortChanged("4242"))

        val state = viewModel.uiState.value
        assertEquals("192.168.1.10", state.ip)
        assertEquals("4242", state.port)
    }

    @Test
    fun `when start clicked without role then role error is shown`() = runTest {
        keepCollecting(viewModel.uiState)

        viewModel.onEvent(LobbyEvent.StartClicked)

        assertEquals(LobbyError.RoleNotChosen, viewModel.uiState.value.error)
        assertNull(session.role.value)
        assertNull(session.link.value)
    }

    @Test
    fun `when start clicked with invalid port then port error is shown`() = runTest {
        keepCollecting(viewModel.uiState)

        viewModel.onEvent(LobbyEvent.RoleSelected(Role.CHARACTER))
        viewModel.onEvent(LobbyEvent.PortChanged("pas-un-port"))
        viewModel.onEvent(LobbyEvent.StartClicked)

        assertEquals(LobbyError.InvalidPort, viewModel.uiState.value.error)
        assertNull(session.link.value)
    }

    @Test
    fun `when start clicked as character then session starts with host link`() = runTest {
        keepCollecting(viewModel.uiState)

        viewModel.onEvent(LobbyEvent.RoleSelected(Role.CHARACTER))
        viewModel.onEvent(LobbyEvent.PortChanged("4242"))
        viewModel.onEvent(LobbyEvent.StartClicked)

        assertEquals(Role.CHARACTER, session.role.value)
        assertSame(factory.pair.a, session.link.value)
    }

    @Test
    fun `when start clicked as environment then session starts with join link`() = runTest {
        keepCollecting(viewModel.uiState)

        viewModel.onEvent(LobbyEvent.RoleSelected(Role.ENVIRONMENT))
        viewModel.onEvent(LobbyEvent.IpChanged("192.168.1.10"))
        viewModel.onEvent(LobbyEvent.PortChanged("4242"))
        viewModel.onEvent(LobbyEvent.StartClicked)

        assertEquals(Role.ENVIRONMENT, session.role.value)
        assertSame(factory.pair.b, session.link.value)
    }

    @Test
    fun `when simulated partner clicked then session starts with in-memory link`() = runTest {
        keepCollecting(viewModel.uiState)

        viewModel.onEvent(LobbyEvent.RoleSelected(Role.CHARACTER))
        viewModel.onEvent(LobbyEvent.SimulatedPartnerClicked)

        assertEquals(Role.CHARACTER, session.role.value)
        assertNotNull(session.link.value)
        assertNotSame(factory.pair.a, session.link.value)
        assertEquals(LinkState.CONNECTED, session.link.value?.state?.value)
    }

    @Test
    fun `when simulated partner clicked without role then role error is shown`() = runTest {
        keepCollecting(viewModel.uiState)

        viewModel.onEvent(LobbyEvent.SimulatedPartnerClicked)

        assertEquals(LobbyError.RoleNotChosen, viewModel.uiState.value.error)
        assertNull(session.link.value)
    }

    @Test
    fun `when text changes after error then error is cleared`() = runTest {
        keepCollecting(viewModel.uiState)

        viewModel.onEvent(LobbyEvent.StartClicked)
        viewModel.onEvent(LobbyEvent.PortChanged("4242"))

        assertNull(viewModel.uiState.value.error)
    }
}
