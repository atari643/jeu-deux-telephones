package com.example.app.ui.game

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import com.example.app.game.contract.PlayerState
import com.example.app.game.engine.Level

/**
 * Dessin 2D du niveau (feature 04-rendu).
 *
 * Sémantique du moteur : le joueur est un point situé à ses pieds
 * (voir [com.example.app.game.engine.GameEngine]) ; le carré dessiné a
 * donc ses pieds en bas et son centre horizontal sur `player.x`.
 */

object GameColors {
    val Solid: Color = Color(0xFF555555)
    val Player: Color = Color(0xFF0066CC)
}

/** Niveau par défaut dessiné en 04-rendu (la géométrie réelle arrive avec la feature 05). */
val DEFAULT_LEVEL: Level = Level(listOf("..........", "#S......E#", "##########"))

/** Le point du joueur est à ses pieds : décalage horizontal du centre du carré, en tuiles. */
private const val PLAYER_HALF_WIDTH_TILES = 0.5

/** Le point du joueur est à ses pieds : hauteur du carré, en tuiles. */
private const val PLAYER_HEIGHT_TILES = 1.0

/**
 * Dessine [level] + [player] dans le viewport, en appliquant la rotation du monde
 * `worldAngleDeg` autour du centre de l'écran (rotation du téléphone B, CA4).
 * Le niveau est centré et dimensionné pour tenir dans le plus petit côté.
 */
fun DrawScope.drawGame(
    level: Level,
    player: PlayerState,
    worldAngleDeg: Double
) {
    val tileSize = size.minDimension / maxOf(level.width, level.height)
    val origin = Offset(
        (size.width - level.width * tileSize) / 2f,
        (size.height - level.height * tileSize) / 2f
    )
    rotate(degrees = worldAngleDeg.toFloat(), pivot = center) {
        for (y in 0 until level.height) {
            for (x in 0 until level.width) {
                if (level.isSolid(x, y)) {
                    drawRect(
                        color = GameColors.Solid,
                        topLeft = origin + Offset(x * tileSize, y * tileSize),
                        size = Size(tileSize, tileSize)
                    )
                }
            }
        }
        drawRect(
            color = GameColors.Player,
            topLeft =
                origin +
                    Offset(
                        ((player.x - PLAYER_HALF_WIDTH_TILES) * tileSize).toFloat(),
                        ((player.y - PLAYER_HEIGHT_TILES) * tileSize).toFloat()
                    ),
            size = Size(tileSize, tileSize)
        )
    }
}
