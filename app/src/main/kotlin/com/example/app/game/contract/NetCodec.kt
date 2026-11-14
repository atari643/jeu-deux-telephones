package com.example.app.game.contract

import kotlinx.serialization.json.Json

/**
 * Encode un [NetMessage] en une ligne JSON (sans retour chariot) et décode l'inverse.
 * `ignoreUnknownKeys` pour rester compatible si un champ est ajouté à un bout seulement.
 */
object NetCodec {

    private val json: Json =
        Json {
            ignoreUnknownKeys = true
        }

    /** Produit la ligne JSON à envoyer sur le lien. */
    fun encode(message: NetMessage): String =
        json.encodeToString(NetMessage.serializer(), message)

    /** Retrouve le message d'origine depuis sa ligne JSON. */
    fun decode(line: String): NetMessage =
        json.decodeFromString(NetMessage.serializer(), line)
}
