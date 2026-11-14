package com.example.app.game.engine

import com.example.app.game.contract.GameState
import com.example.app.game.contract.Inputs
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * CA6 : déterminisme — même suite d'Inputs rejouée deux fois -> états
 * strictement identiques, pas à pas. [GameEngine] n'a ni horloge, ni random,
 * ni état mutable : le même enchaînement produit toujours la même suite de
 * [GameState] (comparaison stricte, sans tolérance).
 */
class GameEngineDeterminismTest {

    /** Niveau avec un mur au milieu : la suite d'inputs déclenche collisions, sauts et rotations. */
    private val level = TestLevels.withWall

    private fun scenario(): List<Inputs> =
        (1..SCENARIO_STEPS).map { i ->
            Inputs(
                moveX = when ((i - 1) % 3) {
                    0 -> -1.0
                    1 -> 1.0
                    else -> 0.0
                },
                jump = i % JUMP_EVERY == 1,
                worldAngleDeg = when ((i - 1) / PHASE_LENGTH) {
                    0 -> 0.0
                    1 -> 90.0
                    else -> -45.0
                }
            )
        }

    /** Rejoue [inputs] depuis l'état de départ et renvoie TOUS les états (pas à pas). */
    private fun replay(engine: GameEngine, inputs: List<Inputs>): List<GameState> {
        var s = TestLevels.startFor(level)
        val trace = mutableListOf(s)
        for (input in inputs) {
            s = engine.step(s, input)
            trace += s
        }
        return trace
    }

    @Test
    fun `when same inputs replayed twice then states are strictly identical step by step (CA6)`() {
        val inputs = scenario()
        val first = replay(GameEngine(level), inputs)
        val second = replay(GameEngine(level), inputs)

        assertEquals(
            "les deux runs doivent produire le même nombre de pas",
            first.size,
            second.size
        )
        first.forEachIndexed { i, state ->
            assertEquals(
                "les états au pas ${i + 1} doivent être strictement identiques",
                state,
                second[i]
            )
        }
    }

    @Test
    fun `when the same engine instance replays the sequence then each step is identical (CA6)`() {
        // Un même moteur ne doit porter aucun état caché : un second
        // enchaînement (même point de départ) redonne exactement la même suite.
        val engine = GameEngine(level)
        val inputs = scenario()
        val first = replay(engine, inputs)
        val second = replay(engine, inputs)
        first.forEachIndexed { i, state ->
            assertEquals(
                "rejouer sur le même moteur doit redonner le même état au pas ${i + 1}",
                state,
                second[i]
            )
        }
    }

    private companion object {
        private const val SCENARIO_STEPS = 180
        private const val JUMP_EVERY = 20
        private const val PHASE_LENGTH = 60
    }
}
