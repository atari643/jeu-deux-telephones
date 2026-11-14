package com.example.app.game.contract

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class NetCodecTest {

    private val messages: List<NetMessage> =
        listOf(
            NetMessage.Hello(Role.CHARACTER),
            NetMessage.Hello(Role.ENVIRONMENT),
            NetMessage.Ready,
            NetMessage.CharInput(seq = 3L, moveX = -1.0, jump = true),
            NetMessage.EnvInput(seq = 9L, angleDeg = -45.0, special = true),
            NetMessage.Snapshot(
                GameState(
                    tick = 7L,
                    player = PlayerState(x = 1.0, y = 2.0, vx = 0.5, vy = -0.5, onGround = false),
                    worldAngleDeg = 30.0,
                    levelId = "level-1",
                    status = GameStatus.RUNNING
                )
            ),
            NetMessage.Ping(id = "p1"),
            NetMessage.Pong(id = "p1")
        )

    @Test
    fun `given each NetMessage when encoded then one line of JSON without newline (CA2)`() {
        for (message in messages) {
            val line = NetCodec.encode(message)
            assertFalse(
                "encoded message must be a single line without newline: $line",
                line.contains('\n')
            )
        }
    }

    @Test
    fun `given each NetMessage when encoded then decoded returns the same message (CA2)`() {
        for (message in messages) {
            assertEquals(message, NetCodec.decode(NetCodec.encode(message)))
        }
    }

    @Test
    fun `given a Snapshot message when decoded then the inner state is preserved (CA2)`() {
        val state =
            GameState(
                tick = 42L,
                player = PlayerState(x = -0.5, y = 3.25, vx = 0.0, vy = 2.0, onGround = true),
                worldAngleDeg = 90.0,
                levelId = "level-2",
                status = GameStatus.WON
            )
        val decoded = NetCodec.decode(NetCodec.encode(NetMessage.Snapshot(state)))
        assertEquals(NetMessage.Snapshot(state), decoded)
    }

    @Test
    fun `Role exposes exactly the two contract values (CA2)`() {
        assertEquals(listOf(Role.CHARACTER, Role.ENVIRONMENT), Role.entries)
    }
}
