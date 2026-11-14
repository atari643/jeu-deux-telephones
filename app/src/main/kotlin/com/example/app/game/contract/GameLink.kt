package com.example.app.game.contract

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

/**
 * État de la liaison entre les deux téléphones.
 */
enum class LinkState {
    CONNECTING,
    CONNECTED,
    DISCONNECTED
}

/**
 * Liaison bidirectionnelle entre les deux téléphones.
 * Les messages arrivent sur [incoming] dans l'ordre où ils ont été envoyés
 * (un par [send]) ; [close] ferme la liaison et termine [incoming].
 */
interface GameLink {
    val state: StateFlow<LinkState>
    val incoming: Flow<NetMessage>

    suspend fun send(message: NetMessage)
    suspend fun close()
}

/**
 * Fabrique de [GameLink] : [host] écoute un port (téléphone personnage,
 * hôte de la simulation), [join] se connecte à un hôte déjà à l'écoute.
 */
interface GameLinkFactory {
    suspend fun host(port: Int): GameLink
    suspend fun join(ip: String, port: Int): GameLink
}
