package com.example.app.game.engine

import com.example.app.game.contract.GameState
import com.example.app.game.contract.GameStatus
import com.example.app.game.contract.Inputs
import com.example.app.game.contract.PlayerState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * CA3 : sans sol sous les pieds, le joueur tombe ;
 * sorti du bas du niveau -> status = LOST.
 */
class GameEngineGravityTest {

    private val engine = GameEngine(TestLevels.flatWithRavine)

    private fun run(state: GameState, inputs: Inputs, steps: Int): GameState =
        (1..steps).fold(state) { s, _ -> engine.step(s, inputs) }

    @Test
    fun `when player on flat ground then falls and lands (CA3)`() {
        val start = TestLevels.startFor(TestLevels.flatWithRavine)
        // Départ y = 1.5 (centre de la rangée 1), sol à y = 2.0
        val after = run(start, Inputs(), steps = 30)
        assertTrue("joueur doit être au sol après la chute", after.player.onGround)
        assertEquals("joueur doit être arrêté sur le dessus du sol", 2.0, after.player.y, 0.01)
    }

    @Test
    fun `when no ground below then player keeps falling (CA3)`() {
        // Joueur au-dessus du ravin (x = 4.5, trou x=3..6 en rangée 2)
        val start = TestLevels.startFor(TestLevels.flatWithRavine).copy(
            player = PlayerState(x = 4.5, y = 1.5, vx = 0.0, vy = 0.0, onGround = false)
        )
        val after = run(start, Inputs(), steps = 5)
        assertTrue(
            "joueur doit tomber (y augmente) sans sol sous les pieds",
            after.player.y > start.player.y
        )
    }

    @Test
    fun `when player falls out of the level then status is LOST (CA3)`() {
        // Joueur au-dessus du ravin : tombe à travers le trou et sort du niveau
        val start = TestLevels.startFor(TestLevels.flatWithRavine).copy(
            player = PlayerState(x = 4.5, y = 1.5, vx = 0.0, vy = 0.0, onGround = false)
        )
        val after = run(start, Inputs(), steps = 60)
        assertEquals("sorti du bas du niveau -> LOST", GameStatus.LOST, after.status)
    }
}
