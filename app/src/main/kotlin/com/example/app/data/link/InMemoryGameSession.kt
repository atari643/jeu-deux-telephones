package com.example.app.data.link

import com.example.app.game.contract.GameLink
import com.example.app.game.contract.GameSession
import com.example.app.game.contract.Role
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * [GameSession] en mémoire : le lobby la remplit via [start], l'écran de jeu
 * la lit. [end] ferme la liaison éventuelle et remet la session à zéro.
 */
class InMemoryGameSession : GameSession {

    private val _role = MutableStateFlow<Role?>(null)
    private val _link = MutableStateFlow<GameLink?>(null)

    override val role: StateFlow<Role?> = _role.asStateFlow()

    override val link: StateFlow<GameLink?> = _link.asStateFlow()

    override fun start(role: Role, link: GameLink) {
        _role.value = role
        _link.value = link
    }

    override suspend fun end() {
        _link.value?.close()
        _link.value = null
        _role.value = null
    }
}
