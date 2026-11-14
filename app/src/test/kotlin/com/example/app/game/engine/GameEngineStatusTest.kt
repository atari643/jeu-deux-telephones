package com.example.app.game.engine

import com.example.app.game.contract.GameState
import com.example.app.game.contract.GameStatus
import com.example.app.game.contract.Inputs
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * CA3 : le joueur tombe dans le ravin et sort du bas du niveau -> status = LOST.
 * CA5 : toucher la tuile de sortie -> status = WON ;
 * en WON/LOST, `step` ne change plus rien sauf le tick.
 */
class GameEngineStatusTest {

    private val engineFlat = GameEngine(TestLevels.flat)
    private val engineRavine = GameEngine(TestLevels.flatWithRavine)

    private fun run(engine: GameEngine, state: GameState, inputs: Inputs, steps: Int): GameState =
        (1..steps).fold(state) { s, _ -> engine.step(s, inputs) }

    @Test
    fun `when player walks into the ravine then status is LOST (CA3)`() {
        val start = TestLevels.startFor(TestLevels.flatWithRavine)
        val after = run(engineRavine, start, Inputs(moveX = 1.0), steps = 60)
        assertEquals(
            "chute dans le ravin, sortie du bas du niveau -> LOST",
            GameStatus.LOST,
            after.status
        )
    }

    @Test
    fun `when player touches the exit tile then status is WON (CA5)`() {
        val start = TestLevels.startFor(TestLevels.flat)
        val after = run(engineFlat, start, Inputs(moveX = 1.0), steps = 120)
        assertEquals("toucher la tuile de sortie -> WON", GameStatus.WON, after.status)
    }

    @Test
    fun `when status is WON then step changes only the tick (CA5)`() {
        val start = TestLevels.startFor(TestLevels.flat)
        val won = run(engineFlat, start, Inputs(moveX = 1.0), steps = 120)
        assertEquals(GameStatus.WON, won.status)
        val after = engineFlat.step(won, Inputs(moveX = 1.0, jump = true, worldAngleDeg = 90.0))
        assertEquals("seul le tick doit avancer", won.tick + 1, after.tick)
        assertEquals("le joueur doit être figé en WON", won.player, after.player)
        assertEquals(GameStatus.WON, after.status)
    }

    @Test
    fun `when status is LOST then step changes only the tick (CA5)`() {
        val start = TestLevels.startFor(TestLevels.flatWithRavine)
        val lost = run(engineRavine, start, Inputs(moveX = 1.0), steps = 60)
        assertEquals(GameStatus.LOST, lost.status)
        val after = engineRavine.step(lost, Inputs(moveX = 1.0, jump = true, worldAngleDeg = 90.0))
        assertEquals("seul le tick doit avancer", lost.tick + 1, after.tick)
        assertEquals("le joueur doit être figé en LOST", lost.player, after.player)
        assertEquals(GameStatus.LOST, after.status)
    }
}
