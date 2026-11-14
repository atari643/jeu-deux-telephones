package com.example.app.game.engine

import kotlin.math.cos
import kotlin.math.sin

/**
 * Rotation du monde (CA4) : le niveau ne bouge pas, seul le vecteur gravité
 * tourne avec l'angle du monde imposé par le téléphone B (`Inputs.worldAngleDeg`).
 */
object Gravity {

    /**
     * Vecteur gravité (tuiles/s²) pour un angle de monde [angleDeg] en degrés.
     * 0° -> (0, +GRAVITY) : vers le bas. 90° -> (+GRAVITY, 0) : vers la droite.
     * Les angles positifs tournent dans le sens horaire (l'axe y pointe vers le bas).
     */
    fun vector(angleDeg: Double): GravityVector {
        val rad = angleDeg * GameConfig.DEG_TO_RAD
        return GravityVector(
            x = GameConfig.GRAVITY * sin(rad),
            y = GameConfig.GRAVITY * cos(rad)
        )
    }
}

/** Vecteur gravité en tuiles/s² (x vers la droite, y vers le bas). */
data class GravityVector(val x: Double, val y: Double)
