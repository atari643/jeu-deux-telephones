package com.example.app.game.engine

import com.example.app.game.contract.GameState
import com.example.app.game.contract.Inputs
import com.example.app.game.contract.PlayerState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * CA4 : `worldAngleDeg = 90` -> la gravité est tournée de 90° (le joueur
 * « tombe » sur le côté). Le niveau ne bouge pas : seul le vecteur gravité
 * tourne ([Gravity.vector]).
 */
class GameEngineRotationTest {

    private val engine = GameEngine(TestLevels.withWallColumn)

    private fun run(state: GameState, inputs: Inputs, steps: Int): GameState =
        (1..steps).fold(state) { s, _ -> engine.step(s, inputs) }

    /** Joueur en l'air au-dessus du ravin : aucune paroi verticale avant x = 9. */
    private val airborne = TestLevels.startFor(TestLevels.withWallColumn).copy(
        player = PlayerState(x = 4.5, y = 1.5, vx = 0.0, vy = 0.0, onGround = false)
    )

    @Test
    fun `gravity vector follows the world angle (CA4)`() {
        assertEquals("à 0° la gravité est nulle sur x", 0.0, Gravity.vector(0.0).x, 1e-9)
        assertEquals("à 0° la gravité est pleine vers le bas", GameConfig.GRAVITY, Gravity.vector(0.0).y, 1e-9)
        assertEquals("à 90° la gravité est pleine vers la droite", GameConfig.GRAVITY, Gravity.vector(90.0).x, 1e-9)
        assertEquals("à 90° la gravité est nulle sur y", 0.0, Gravity.vector(90.0).y, 1e-9)
        assertEquals("à 180° la gravité est vers le haut", -GameConfig.GRAVITY, Gravity.vector(180.0).y, 1e-9)
    }

    @Test
    fun `when worldAngleDeg 90 then player falls to the side (CA4)`() {
        val after = run(airborne, Inputs(worldAngleDeg = 90.0), steps = 30)
        assertTrue(
            "à 90° la gravité pousse vers la droite : x augmente",
            after.player.x > airborne.player.x + 2.0
        )
        assertEquals(
            "à 90° la composante verticale de la gravité est nulle : y ne change pas",
            airborne.player.y,
            after.player.y,
            0.01
        )
    }

    @Test
    fun `when worldAngleDeg 90 then fall to the side is stopped by the wall (CA4)`() {
        val after = run(airborne, Inputs(worldAngleDeg = 90.0), steps = 120)
        assertTrue("le joueur ne doit pas traverser le mur (x = 9)", after.player.x <= 9.0)
        assertEquals("le joueur doit rester collé au mur", 9.0, after.player.x, 0.01)
    }

    @Test
    fun `when worldAngleDeg 0 then gravity stays vertical (CA4)`() {
        val after = run(airborne, Inputs(), steps = 5)
        assertEquals(
            "à 0° la gravité ne pousse pas sur x : x ne change pas",
            airborne.player.x,
            after.player.x,
            0.001
        )
        assertTrue("à 0° le joueur tombe toujours vers le bas (y augmente)", after.player.y > airborne.player.y)
    }
}
