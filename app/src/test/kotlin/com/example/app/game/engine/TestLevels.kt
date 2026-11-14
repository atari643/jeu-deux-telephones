package com.example.app.game.engine

import com.example.app.game.contract.GameState
import com.example.app.game.contract.GameStatus
import com.example.app.game.contract.PlayerState

/**
 * Niveaux de test minuscules (3-5 lignes) pour les tests du moteur.
 * Convention : `#` mur, `.` vide, `S` départ, `E` sortie.
 * La position du joueur dans un [startFor] est au CENTRE de la tuile de
 * départ (x + 0.5, y + 0.5).
 */
object TestLevels {

    /** Sol plat avec murs aux deux bouts (sauts, gravité, déplacement). */
    val flat: Level =
        Level(
            listOf(
                "..........",
                "#S......E#",
                "##########"
            )
        )

    /** Sol avec un ravin (x = 3..6) qui mène au vide, pour la chute (CA3). */
    val flatWithRavine: Level =
        Level(
            listOf(
                "..........",
                "#S......E#",
                "###....###"
            )
        )

    /** Un mur plein au milieu (x = 4) pour les collisions horizontales (CA1). */
    val withWall: Level =
        Level(
            listOf(
                "....#.....",
                "#S..#...E#",
                "##########"
            )
        )

    /** État de départ (RUNNING, joueur au centre de la tuile [Level.start]). */
    fun startFor(level: Level): GameState =
        GameState(
            tick = 0L,
            player =
                PlayerState(
                    x = level.start.x + 0.5,
                    y = level.start.y + 0.5,
                    vx = 0.0,
                    vy = 0.0,
                    onGround = true
                ),
            worldAngleDeg = 0.0,
            levelId = "test",
            status = GameStatus.RUNNING
        )

    /** État de départ sur [flatWithRavine], utilisé par les tests du moteur. */
    val start: GameState = startFor(flatWithRavine)
}
