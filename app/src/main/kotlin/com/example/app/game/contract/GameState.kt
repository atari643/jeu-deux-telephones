package com.example.app.game.contract

import kotlinx.serialization.Serializable

/**
 * État d'un joueur. Unités : tuiles, axe y vers le bas.
 */
@Serializable
data class PlayerState(val x: Double, val y: Double, val vx: Double, val vy: Double, val onGround: Boolean)

/**
 * État complet du jeu, immuable, recalculé pas à pas par le moteur.
 */
@Serializable
data class GameState(val tick: Long, val player: PlayerState, val worldAngleDeg: Double, val levelId: String, val status: GameStatus)

/**
 * Phase de la partie.
 */
@Serializable
enum class GameStatus {
    WAITING,
    RUNNING,
    WON,
    LOST
}
