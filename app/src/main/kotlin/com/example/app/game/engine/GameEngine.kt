package com.example.app.game.engine

import com.example.app.game.contract.Engine
import com.example.app.game.contract.GameState
import com.example.app.game.contract.GameStatus
import com.example.app.game.contract.Inputs
import com.example.app.game.contract.PlayerState
import kotlin.math.floor

/**
 * Moteur du jeu : pas fixe, gravité, collisions horizontales et verticales
 * avec les tuiles.
 *
 * Le saut (CA2) arrive dans la tâche 4 ; la rotation du monde (CA4) dans la
 * tâche 5.
 */
class GameEngine(private val level: Level) : Engine {

    override fun step(state: GameState, inputs: Inputs): GameState {
        // En WON / LOST, plus rien ne change sauf le tick.
        if (state.status != GameStatus.RUNNING) {
            return state.copy(tick = state.tick + 1)
        }

        val dt = GameConfig.STEP_SECONDS
        val p = state.player

        // --- Gravité (toujours vers le bas ; rotation = tâche 5) ---
        val newVy = (p.vy + GameConfig.GRAVITY * dt).coerceAtMost(GameConfig.MAX_FALL_SPEED)

        // --- Nouvelle position ---
        val newVx = inputs.moveX * GameConfig.RUN_SPEED
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
