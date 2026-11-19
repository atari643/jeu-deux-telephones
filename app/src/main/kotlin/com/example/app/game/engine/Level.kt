package com.example.app.game.engine

/**
 * Position d'une tuile en coordonnées entières du niveau.
 * x croît vers la droite, y croît vers le bas (unités : tuiles).
 */
data class Tile(val x: Int, val y: Int)

/**
 * Niveau de tuiles du moteur (interne, pas dans le contrat).
 *
 * Construit à partir de rangées de caractères, une ligne = une rangée de tuiles :
 * - `#` : mur / sol solide
 * - `S` : départ du joueur (exactly one)
 * - `E` : sortie (exactly one)
 * - tout autre caractère (notamment `.` et l'espace) : vide
 *
 * Les coordonnées sont en tuiles. Une position hors des bornes n'est PAS
 * solide : un joueur peut donc tomber hors du bas du niveau (CA3) ; les niveaux
 * de test entourent le terrain de jeu de murs `#` pour l'empêcher de sortir
 * des côtés et du dessus.
 */
class Level(private val rows: List<String>) {

    val width: Int
    val height: Int
    val start: Tile
    val exit: Tile
    private val solid: Array<BooleanArray>

    init {
        require(rows.isNotEmpty()) { "un niveau doit contenir au moins une rangée" }
        val w = rows.maxOf { it.length }
        require(w > 0) { "un niveau doit avoir une largeur > 0" }
        val h = rows.size
        width = w
        height = h
        solid = Array(h) { BooleanArray(w) }
        var s: Tile? = null
        var e: Tile? = null
        for (y in 0 until h) {
            for (x in 0 until w) {
                val c = if (x < rows[y].length) rows[y][x] else ' '
                when (c) {
                    '#' -> solid[y][x] = true

                    'S' -> {
                        require(s == null) { "un niveau doit contenir exactement un départ (S) : $rows" }
                        s = Tile(x, y)
                    }

                    'E' -> {
                        require(e == null) { "un niveau doit contenir exactement une sortie (E) : $rows" }
                        e = Tile(x, y)
                    }
                }
            }
        }
        start = requireNotNull(s) { "niveau sans départ (S) : $rows" }
        exit = requireNotNull(e) { "niveau sans sortie (E) : $rows" }
    }

    /**
     * Vrai si la tuile entière (x, y) est un mur solide.
     * Hors des bornes du niveau : faux (vide), donc on peut en tomber.
     */
    fun isSolid(x: Int, y: Int): Boolean =
        if (x in 0 until width && y in 0 until height) solid[y][x] else false
}
