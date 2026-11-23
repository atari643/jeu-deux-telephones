package com.example.app.ui.game

import com.example.app.game.engine.GameConfig
import com.example.app.game.engine.GameEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * 04-rendu CA6 : la logique du simulateur PC (classe [GamePlayground] pur
 * Kotlin) est testée ici : clics -> Inputs -> etat du VRAI [GameEngine]
 * avance a pas fixe sur le niveau de test [DEFAULT_LEVEL].
 */
class GamePlaygroundTest {

    private val stepNanos: Long = (GameConfig.STEP_SECONDS * 1_000_000_000L).toLong()

    private fun newPlayground() = GamePlayground(GameEngine(DEFAULT_LEVEL), DEFAULT_LEVEL)

    /** Premier [GamePlayground.onFrame] a t = 0 (reference), puis un pas par image. */
    private fun runSteps(playground: GamePlayground, steps: Int) {
        var timeNanos = 0L
        playground.onFrame(timeNanos)
        repeat(steps) {
            timeNanos += stepNanos
            playground.onFrame(timeNanos)
        }
    }

    @Test
    fun `left click builds a negative moveX input`() {
        val playground = newPlayground()
        playground.left()
        assertEquals(-1.0, playground.inputs().moveX, 0.001)
    }

    @Test
    fun `right click builds a positive moveX input and left click replaces it`() {
        val playground = newPlayground()
        playground.right()
        assertEquals(1.0, playground.inputs().moveX, 0.001)
        playground.left()
        assertEquals(-1.0, playground.inputs().moveX, 0.001)
    }

    @Test
    fun `jump click toggles the jump input`() {
        val playground = newPlayground()
        playground.jump()
        assertTrue(playground.inputs().jump)
        playground.jump()
        assertFalse(playground.inputs().jump)
    }

    @Test
    fun `rotate buttons change the world angle by 15 degrees each`() {
        val playground = newPlayground()
        playground.rotateLeft()
        assertEquals(-15.0, playground.worldAngleDeg, 0.001)
        playground.rotateRight()
        playground.rotateRight()
        assertEquals(15.0, playground.worldAngleDeg, 0.001)
    }

    @Test
    fun `left click then frames moves the player left`() {
        val playground = newPlayground()
        val x0 = playground.state.player.x
        playground.left()
        runSteps(playground, 4)
        assertTrue(playground.state.player.x < x0)
    }

    @Test
    fun `right click then frames moves the player right`() {
        val playground = newPlayground()
        val x0 = playground.state.player.x
        playground.right()
        runSteps(playground, 4)
        assertTrue(playground.state.player.x > x0)
    }

    @Test
    fun `jump click on ground then one step raises the player`() {
        val playground = newPlayground()
        val y0 = playground.state.player.y
        playground.jump()
        runSteps(playground, 1)
        assertTrue(playground.state.player.y < y0)
    }

    @Test
    fun `a frame spanning three fixed steps advances the engine exactly three times`() {
        val playground = newPlayground()
        playground.onFrame(0L)
        playground.onFrame(3L * stepNanos)
        assertEquals(3L, playground.state.tick)
    }

    @Test
    fun `without clicks the player stays on the start tile`() {
        val playground = newPlayground()
        val x0 = playground.state.player.x
        val y0 = playground.state.player.y
        runSteps(playground, 30)
        assertEquals(x0, playground.state.player.x, 0.001)
        assertEquals(y0, playground.state.player.y, 0.001)
    }
}
