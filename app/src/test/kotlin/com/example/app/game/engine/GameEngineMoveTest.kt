package com.example.app.game.engine

import com.example.app.game.contract.GameState
import com.example.app.game.contract.Inputs
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * CA1 : `moveX = 1` pendant 60 pas -> x augmente d'environ `RUN_SPEED` ;
 * contre un mur, x ne dépasse pas le mur.
 */
class GameEngineMoveTest {

    private val engine = GameEngine(TestLevels.flat)
    private val engineWithWall = GameEngine(TestLevels.withWall)

    private fun run(engine: GameEngine, state: GameState, inputs: Inputs, steps: Int): GameState =
        (1..steps).fold(state) { s, _ -> engine.step(s, inputs) }

    @Test
    fun `when moveX 1 for 60 steps then x increases by about RUN_SPEED (CA1)`() {
        val start = TestLevels.startFor(TestLevels.flat)
        val after = run(engine, start, Inputs(moveX = 1.0), steps = 60)
        assertEquals(
            "x doit avoir augmenté d'environ RUN_SPEED",
            start.player.x + GameConfig.RUN_SPEED,
            after.player.x,
            0.1
        )
    }

    @Test
    fun `when moving right into a wall then x does not pass the wall (CA1)`() {
        val start = TestLevels.startFor(TestLevels.withWall)
        val after = run(engineWithWall, start, Inputs(moveX = 1.0), steps = 120)
        assertTrue("x ne doit jamais dépasser la face gauche du mur", after.player.x <= 4.0)
        assertEquals("le joueur doit rester collé au mur", 4.0, after.player.x, 0.01)
    }
}
