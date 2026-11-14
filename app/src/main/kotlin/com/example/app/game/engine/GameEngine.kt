package com.example.app.game.engine

import com.example.app.game.contract.Engine
import com.example.app.game.contract.GameState
import com.example.app.game.contract.GameStatus
import com.example.app.game.contract.Inputs
import com.example.app.game.contract.PlayerState
import kotlin.math.floor

/**
 * Moteur du jeu : pas fixe, gravité, saut (CA2), collisions horizontales et
 * verticales avec les tuiles, rotation du monde (CA4).
 *
 * Le niveau ne bouge pas : la gravité est tournée par [Gravity.vector] selon
 * `inputs.worldAngleDeg`.
 */
class GameEngine(private val level: Level) : Engine {

    override fun step(state: GameState, inputs: Inputs): GameState {
        // En WON / LOST, plus rien ne change sauf le tick.
        if (state.status != GameStatus.RUNNING) {
            return state.copy(tick = state.tick + 1)
        }

        val dt = GameConfig.STEP_SECONDS
        val p = state.player

        // --- Gravité tournée par l'angle du monde (CA4) : le niveau ne bouge pas ---
        val g = Gravity.vector(inputs.worldAngleDeg)

        // --- Vertical (CA3) : gravité tournée, accumulée, bornée ---
        val vyAfterGravity =
            (p.vy + g.y * dt).coerceIn(-GameConfig.MAX_FALL_SPEED, GameConfig.MAX_FALL_SPEED)

        // --- Saut (CA2) : uniquement depuis le sol ; ignoré en l'air ---
        val newVy = if (inputs.jump && p.onGround) -GameConfig.JUMP_SPEED else vyAfterGravity

        // --- Horizontal (CA1 + CA4) : la course pilote la vitesse ; sans course,
        //     la composante horizontale de la gravité fait « tomber » sur le côté ---
        val newVx = when {
            inputs.moveX != 0.0 -> inputs.moveX * GameConfig.RUN_SPEED
            g.x == 0.0 -> 0.0
            else -> (p.vx + g.x * dt).coerceIn(-GameConfig.MAX_FALL_SPEED, GameConfig.MAX_FALL_SPEED)
        }

        // --- Nouvelle position ---
        val newY = p.y + newVy * dt

        // --- Résolution de la collision horizontale (CA1) ---
        val (resolvedX, hitWall) = Collisions.resolveHorizontal(level, p.x, p.x + newVx * dt, p.y)

        // --- Résolution de la collision verticale ---
        var resolvedY = newY
        var resolvedVy = newVy
        var onGround = false
        val tileX = floor(resolvedX).toInt()
        val tileY = floor(newY).toInt()

        if (level.isSolid(tileX, tileY)) {
            if (newVy >= 0.0) {
                // Chute : on repousse le joueur sur le dessus de la tuile.
                resolvedY = tileY.toDouble()
                resolvedVy = 0.0
                onGround = true
            } else {
                // Montée (saut contre un plafond) : on repousse sous la tuile.
                resolvedY = (tileY + 1).toDouble()
                resolvedVy = 0.0
            }
        }

        // --- Défaite : sortie du niveau (CA3) ---
        val lost = Collisions.isOutOfBounds(level, resolvedX, resolvedY)

        return state.copy(
            tick = state.tick + 1,
            player = PlayerState(
                x = resolvedX,
                y = resolvedY,
                vx = if (hitWall) 0.0 else newVx,
                vy = resolvedVy,
                onGround = onGround
            ),
            status = if (lost) GameStatus.LOST else state.status
        )
    }
}
