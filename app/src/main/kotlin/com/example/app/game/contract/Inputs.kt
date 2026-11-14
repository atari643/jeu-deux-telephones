package com.example.app.game.contract

import kotlinx.serialization.Serializable

/**
 * Entrées fournies au moteur pour un pas de simulation.
 * `moveX` dans [-1, 1] ; `worldAngleDeg` = rotation du monde demandée par l'environnement.
 */
@Serializable
data class Inputs(
    val moveX: Double = 0.0,
    val jump: Boolean = false,
    val worldAngleDeg: Double = 0.0,
    val darkWorld: Boolean = false,
    val special: Boolean = false
)
