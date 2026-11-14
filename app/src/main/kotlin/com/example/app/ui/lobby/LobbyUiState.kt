package com.example.app.ui.lobby

import com.example.app.game.contract.Role

/** Etat COMPLET de l'ecran, immuable. L'ecran n'affiche que ce qui est ici. */
data class LobbyUiState(val role: Role? = null, val ip: String = "", val port: String = "", val error: LobbyError? = null)

/** Erreurs metier sans Context : l'ecran les traduit en String via stringResource. */
enum class LobbyError { RoleNotChosen, InvalidPort }

/** Tout ce que l'utilisateur peut faire sur l'ecran. */
sealed interface LobbyEvent {
    data class RoleSelected(val role: Role) : LobbyEvent

    data class IpChanged(val value: String) : LobbyEvent

    data class PortChanged(val value: String) : LobbyEvent

    data object StartClicked : LobbyEvent

    data object SimulatedPartnerClicked : LobbyEvent
}
