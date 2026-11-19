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
     * Résout le déplacement horizontal d'un pas (CA1) : si la position cible
     * [xNew] est dans une tuile solide (y constant), le joueur est repoussé
     * sur la face de la tuile et le déplacement est annulé.
     *
     * @return la paire (x résolu, true si une paroi a été atteinte).
     */
    fun resolveHorizontal(level: Level, xOld: Double, xNew: Double, y: Double): Pair<Double, Boolean> {
        val target = floor(xNew).toInt()
        val collided = xNew != xOld && level.isSolid(target, rowAt(y))
        if (!collided) return xNew to false
        val x = if (xNew > xOld) target.toDouble() else (target + 1).toDouble()
        return x to true
    }

    /**
     * Rangée de tuile sondée pour un déplacement à l'ordonnée y : si y est
     * pile sur une ligne de tuiles (entier), on compte la rangée du dessus,
     * car le joueur est alors posé sur la face haute de la rangée du dessous.
     */
    private fun rowAt(y: Double): Int {
        val row = floor(y).toInt()
        return if (y == row.toDouble()) row - 1 else row
    }

    /**
     * Vrai si la position est sortie des bornes du niveau.
     * Le joueur peut tomber hors du bas (CA3) ; les côtés et le haut sont
     * normalement fermés par des murs, mais on vérifie quand même.
     */
    fun isOutOfBounds(level: Level, x: Double, y: Double): Boolean =
        x < 0.0 || x >= level.width.toDouble() ||
            y < 0.0 || y >= level.height.toDouble()
}
