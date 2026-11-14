package com.example.app.game.engine

import kotlin.math.floor

/**
 * Fonctions de collision tuile du moteur (pur Kotlin, aucun import Android).
 * Les positions du joueur sont en coordonnées continues (tuiles) ;
 * une tuile (tx, ty) occupe [tx, tx+1) × [ty, ty+1).
 */
object Collisions {

    /**
     * Vrai si la position continue (x, y) est dans une tuile solide du niveau.
     */
    fun isSolidAt(level: Level, x: Double, y: Double): Boolean =
        level.isSolid(floor(x).toInt(), floor(y).toInt())

    /**
     * Vrai si la position est sortie des bornes du niveau.
     * Le joueur peut tomber hors du bas (CA3) ; les côtés et le haut sont
     * normalement fermés par des murs, mais on vérifie quand même.
     */
    fun isOutOfBounds(level: Level, x: Double, y: Double): Boolean =
        x < 0.0 || x >= level.width.toDouble() ||
            y < 0.0 || y >= level.height.toDouble()
}
