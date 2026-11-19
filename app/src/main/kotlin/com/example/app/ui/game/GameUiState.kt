package com.example.app.ui.game

import com.example.app.game.contract.GameState
import com.example.app.game.contract.LinkState
import com.example.app.game.contract.Role

/** Etat COMPLET de l'ecran, immuable. L'ecran n'affiche que ce qui est ici. */
data class GameUiState(
    val role: Role? = null,
    val linkState: LinkState? = null,
    /** Dernier etat a afficher : simulation locale (CHARACTER) ou dernier Snapshot (ENVIRONMENT). */
    val game: GameState? = null,
    /**
     * Rotation du monde du role ENVIRONMENT, affichee tout de suite sans attendre le
     * Snapshot (decision 0002) ; null pour le role CHARACTER (on dessine alors
     * [GameState.worldAngleDeg] de [game]).
     */
    val liveWorldAngleDeg: Double? = null,
    val error: GameError? = null
)

/** Erreurs metier sans Context : l'ecran les traduit en String via stringResource. */
enum class GameError { NoSession }

/** Tout ce que l'utilisateur peut faire sur l'ecran. */
sealed interface GameEvent {

    /**
     * Image produite par la boucle de rendu (Route, via withFrameNanos) :
     * le ViewModel accumule le temps et fait avancer le moteur a pas fixe.
     */
    data class Frame(val timeNanos: Long) : GameEvent

    /** Bouton « Rejouer » du panneau de fin de partie (WON/LOST). */
    data object Replay : GameEvent
}
