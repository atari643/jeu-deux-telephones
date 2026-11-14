package com.example.app.game.contract

import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Test

class GameStateTest {

    private val json = Json { ignoreUnknownKeys = false }

    @Test
    fun `given a GameState when encoded to JSON and decoded then same object (CA1)`() {
        val state =
            GameState(
                tick = 42L,
                player = PlayerState(x = 1.5, y = -3.25, vx = 0.0, vy = 2.0, onGround = true),
                worldAngleDeg = 90.0,
                levelId = "level-1",
                status = GameStatus.RUNNING
            )
        val text = json.encodeToString(GameState.serializer(), state)
        val decoded = json.decodeFromString(GameState.serializer(), text)
        assertEquals(state, decoded)
    }

    @Test
    fun `given an Inputs when encoded to JSON and decoded then same object (CA1)`() {
        val inputs =
            Inputs(moveX = -1.0, jump = true, worldAngleDeg = 45.0, darkWorld = true, special = false)
        val text = json.encodeToString(Inputs.serializer(), inputs)
        val decoded = json.decodeFromString(Inputs.serializer(), text)
        assertEquals(inputs, decoded)
    }

    @Test
    fun `GameStatus exposes exactly the four contract values (CA1)`() {
        assertEquals(listOf(GameStatus.WAITING, GameStatus.RUNNING, GameStatus.WON, GameStatus.LOST), GameStatus.entries)
    }

    @Test
    fun `given a state when copied then original stays unchanged (CA1 immutable)`() {
        val state =
            GameState(
                tick = 0L,
                player = PlayerState(x = 0.0, y = 0.0, vx = 0.0, vy = 0.0, onGround = false),
                worldAngleDeg = 0.0,
                levelId = "level-1",
                status = GameStatus.WAITING
            )
        val next = state.copy(tick = state.tick + 1, player = state.player.copy(vx = 1.0))
        assertEquals(0L, state.tick)
        assertEquals(0.0, state.player.vx, 0.0)
        assertEquals(1L, next.tick)
        assertEquals(1.0, next.player.vx, 0.0)
    }
}
