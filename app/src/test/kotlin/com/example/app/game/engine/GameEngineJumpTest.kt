package com.example.app.game.engine

import com.example.app.game.contract.GameState
import com.example.app.game.contract.Inputs
import com.example.app.game.contract.PlayerState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * CA2 : `jump = true` au sol -> y diminue puis le joueur retombe au sol ;
 * `jump` en l'air est ignoré.
 *
 * Le niveau de test est [TestLevels.tall] : l'apex d'un saut complet vaut
 * environ `JUMP_SPEED^2 / (2 * GRAVITY)` = 3 tuiles, il faut donc du plafond.
 */
class GameEngineJumpTest {

    private val engine = GameEngine(TestLevels.tall)

    private fun run(state: GameState, inputs: Inputs, steps: Int): GameState =
        (1..steps).fold(state) { s, _ -> engine.step(s, inputs) }

    @Test
    fun `when jump pressed on ground then player goes up (CA2)`() {
        val start = TestLevels.startFor(TestLevels.tall)
        val settled = run(start, Inputs(), steps = 20)
        assertTrue("le joueur doit être posé au sol avant le saut", settled.player.onGround)
        val after = run(settled, Inputs(jump = true), steps = 10)
        assertTrue(
            "le saut doit faire monter le joueur (y diminue)",
            after.player.y < settled.player.y
        )
    }

    @Test
    fun `when jump then player falls back to the ground (CA2)`() {
        val start = TestLevels.startFor(TestLevels.tall)
        val settled = run(start, Inputs(), steps = 20)
        val jumped = run(settled, Inputs(jump = true), steps = 10)
        assertTrue("après 10 pas de saut, le joueur doit être en l'air", !jumped.player.onGround)
        val after = run(jumped, Inputs(), steps = 90)
        assertTrue("le joueur doit retomber au sol", after.player.onGround)
        assertEquals("le joueur doit être arrêté sur le dessus du sol", 5.0, after.player.y, 0.01)
    }

    @Test
    fun `when jump pressed in the air then ignored (CA2)`() {
        val inAir = TestLevels.startFor(TestLevels.tall).copy(
            player = PlayerState(x = 4.5, y = 3.0, vx = 0.0, vy = 0.0, onGround = false)
        )
        val after = run(inAir, Inputs(jump = true), steps = 5)
        assertTrue(
            "en l'air le saut est ignoré : le joueur continue de tomber (y augmente)",
            after.player.y > inAir.player.y
        )
        assertTrue("la vitesse du saut ne doit pas s'appliquer en l'air", after.player.vy > 0.0)
    }
}
