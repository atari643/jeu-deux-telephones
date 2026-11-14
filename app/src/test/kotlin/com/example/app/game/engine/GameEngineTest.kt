package com.example.app.game.engine

import com.example.app.game.contract.GameState
import com.example.app.game.contract.GameStatus
import com.example.app.game.contract.Inputs
import com.example.app.game.contract.PlayerState
import org.junit.Assert.assertEquals
import org.junit.Test

class GameEngineTest {

    private val engine = GameEngine()

    private val start =
        GameState(
            tick = 0L,
            player = PlayerState(x = 3.0, y = 2.0, vx = 0.0, vy = 0.0, onGround = true),
            worldAngleDeg = 0.0,
            levelId = "test",
            status = GameStatus.RUNNING
        )

    private fun run(steps: Int) = (1..steps).fold(start) { s, _ -> engine.step(s, Inputs()) }

    @Test
    fun `when one step then tick is incremented by one (CA4)`() {
        assertEquals(1L, engine.step(start, Inputs()).tick)
    }

    @Test
    fun `when N steps then tick is incremented N times (CA4)`() {
        assertEquals(42L, run(42).tick)
    }

    @Test
    fun `when steps then player and the rest of the state are unchanged (CA4)`() {
        val after = run(10)
        assertEquals(start, after.copy(tick = start.tick))
    }
}
