package com.example.app.data.link

import com.example.app.game.contract.GameLink
import com.example.app.game.contract.GameLinkFactory

/**
 * [GameLinkFactory] en mémoire, implémentation de départ de la feature 00-contrat :
 * [host] renvoie un bout de la paire, [join] l'autre, reliés par [InMemoryLinkPair].
 * Aucun réseau, aucun port réel. A remplacer par le vrai factory TCP
 * (feature 02-liaison-tcp, propriétaire B).
 */
class InMemoryGameLinkFactory : GameLinkFactory {

    private val pair = InMemoryLinkPair()

    override suspend fun host(port: Int): GameLink = pair.a

    override suspend fun join(ip: String, port: Int): GameLink = pair.b
}
