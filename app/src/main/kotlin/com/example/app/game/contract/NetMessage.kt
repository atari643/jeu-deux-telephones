package com.example.app.game.contract

import kotlinx.serialization.Serializable

/**
 * Rôle du téléphone dans la partie.
 * A = personnage (et hôte de la simulation), B = environnement (rotation du niveau).
 */
@Serializable
enum class Role {
    CHARACTER,
    ENVIRONMENT
}

/**
 * Message échangé entre les deux téléphones. Chaque message est encodé
 * en une ligne JSON par [NetCodec].
 */
@Serializable
sealed class NetMessage {

    /** Première connexion : annonce du rôle de l'émetteur. */
    @Serializable
    data class Hello(val role: Role) : NetMessage()

    /** L'émetteur est prêt (niveau chargé, capteurs démarrés). */
    @Serializable
    object Ready : NetMessage()

    /** Entrées du personnage : `seq` = numéro de pas, `moveX` dans [-1, 1]. */
    @Serializable
    data class CharInput(val seq: Long, val moveX: Double, val jump: Boolean) : NetMessage()

    /** Entrées de l'environnement : `angleDeg` = rotation demandée, `special` = mode sombre. */
    @Serializable
    data class EnvInput(val seq: Long, val angleDeg: Double, val special: Boolean) : NetMessage()

    /** État complet envoyé par l'hôte au téléphone qui ne simule pas. */
    @Serializable
    data class Snapshot(val state: GameState) : NetMessage()

    /** Sonde de latence. */
    @Serializable
    data class Ping(val id: String) : NetMessage()

    /** Réponse à une sonde [Ping]. */
    @Serializable
    data class Pong(val id: String) : NetMessage()
}
