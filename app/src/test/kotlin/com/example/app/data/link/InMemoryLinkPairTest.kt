package com.example.app.data.link

import app.cash.turbine.test
import com.example.app.game.contract.LinkState
import com.example.app.game.contract.NetMessage
import com.example.app.game.contract.Role
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.fail
import org.junit.Test

class InMemoryLinkPairTest {

    private val messages: List<NetMessage> =
        listOf(
            NetMessage.Hello(Role.CHARACTER),
            NetMessage.Ready,
            NetMessage.CharInput(seq = 1L, moveX = -1.0, jump = false),
            NetMessage.EnvInput(seq = 2L, angleDeg = 45.0, special = true),
            NetMessage.Ping(id = "p1"),
            NetMessage.Pong(id = "p1")
        )

    @Test
    fun `given a pair when a sends messages then b receives them in order (CA3)`() =
        runTest {
            val pair = InMemoryLinkPair()
            messages.forEach { pair.a.send(it) }
            pair.b.incoming.test {
                messages.forEach { expected -> assertEquals(expected, awaitItem()) }
            }
        }

    @Test
    fun `given a pair when b sends messages then a receives them in order (CA3)`() =
        runTest {
            val pair = InMemoryLinkPair()
            messages.forEach { pair.b.send(it) }
            pair.a.incoming.test {
                messages.forEach { expected -> assertEquals(expected, awaitItem()) }
            }
        }

    @Test
    fun `given a pair when created then both links are connected`() {
        val pair = InMemoryLinkPair()
        assertEquals(LinkState.CONNECTED, pair.a.state.value)
        assertEquals(LinkState.CONNECTED, pair.b.state.value)
    }

    @Test
    fun `given a pair when one side closes then both states are DISCONNECTED and incoming completes`() =
        runTest {
            val pair = InMemoryLinkPair()
            pair.a.send(NetMessage.Ready)
            pair.a.close()
            assertEquals(LinkState.DISCONNECTED, pair.a.state.value)
            assertEquals(LinkState.DISCONNECTED, pair.b.state.value)
            pair.b.incoming.test {
                assertEquals(NetMessage.Ready, awaitItem())
                awaitComplete()
            }
        }

    @Test
    fun `given a closed link when sending then it throws IllegalStateException`() =
        runTest {
            val pair = InMemoryLinkPair()
            pair.b.close()
            try {
                pair.a.send(NetMessage.Ready)
                fail("send() on a closed link must throw IllegalStateException")
            } catch (expected: IllegalStateException) {
                // attendu : la liaison est fermée
            }
        }

    @Test
    fun `given a session when started then role and link are set`() =
        runTest {
            val pair = InMemoryLinkPair()
            val session = InMemoryGameSession()
            assertNull(session.role.value)
            assertNull(session.link.value)
            session.start(Role.ENVIRONMENT, pair.a)
            assertEquals(Role.ENVIRONMENT, session.role.value)
            assertEquals(pair.a, session.link.value)
        }

    @Test
    fun `given a started session when ended then state is cleared and the link is closed`() =
        runTest {
            val pair = InMemoryLinkPair()
            val session = InMemoryGameSession()
            session.start(Role.CHARACTER, pair.a)
            session.end()
            assertNull(session.role.value)
            assertNull(session.link.value)
            assertEquals(LinkState.DISCONNECTED, pair.a.state.value)
        }
}
