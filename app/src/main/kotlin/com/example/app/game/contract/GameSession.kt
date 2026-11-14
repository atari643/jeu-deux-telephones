package com.example.app.game.contract

import kotlinx.coroutines.flow.StateFlow

/**
 * Session de jeu en cours, partagée entre le lobby (qui la remplit via
 * [start]) et l'écran de jeu (qui la lit). [role] et [link] sont nuls
 * tant qu'aucune session n'a démarré.
 */
interface GameSession {
    val role: StateFlow<Role?>
    val link: StateFlow<GameLink?>

    fun start(role: Role, link: GameLink)
    suspend fun end()
}
