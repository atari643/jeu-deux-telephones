package com.example.app.testing

import com.example.app.data.link.InMemoryLinkPair
import com.example.app.game.contract.GameLink
import com.example.app.game.contract.GameLinkFactory

/**
 * [GameLinkFactory] en mémoire pour les tests : [host] renvoie un bout de la paire
 * et [join] l'autre, reliés par [InMemoryLinkPair]. Aucun réseau, aucun port réel.
 */
class FakeGameLinkFactory : GameLinkFactory {

    /** Paire sous-jacente : les deux bouts sont interconnectés en mémoire. */
    val pair = InMemoryLinkPair()

    override suspend fun host(port: Int): GameLink = pair.a

    override suspend fun join(ip: String, port: Int): GameLink = pair.b
}
