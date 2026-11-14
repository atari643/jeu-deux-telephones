package com.example.app.ui.lobby

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.app.data.link.InMemoryLinkPair
import com.example.app.game.contract.GameLinkFactory
import com.example.app.game.contract.GameSession
import com.example.app.game.contract.Role
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val STOP_TIMEOUT_MS = 5_000L
private const val MAX_PORT = 65_535

class LobbyViewModel(private val factory: GameLinkFactory, private val session: GameSession) : ViewModel() {
    private val form = MutableStateFlow(FormState())

    val uiState: StateFlow<LobbyUiState> =
        form.map { LobbyUiState(role = it.role, ip = it.ip, port = it.port, error = it.error) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), LobbyUiState())

    fun onEvent(event: LobbyEvent) {
        when (event) {
            is LobbyEvent.RoleSelected -> form.update { it.copy(role = event.role, error = null) }
            is LobbyEvent.IpChanged -> form.update { it.copy(ip = event.value, error = null) }
            is LobbyEvent.PortChanged -> form.update { it.copy(port = event.value, error = null) }
            LobbyEvent.StartClicked -> start()
            LobbyEvent.SimulatedPartnerClicked -> startSimulated()
        }
    }

    private fun start() {
        val f = form.value
        val role = f.role
        if (role == null) {
            form.update { it.copy(error = LobbyError.RoleNotChosen) }
            return
        }
        val port = f.port.trim().toIntOrNull()
        if (port == null || port !in 1..MAX_PORT) {
            form.update { it.copy(error = LobbyError.InvalidPort) }
            return
        }
        viewModelScope.launch {
            val link = if (role == Role.CHARACTER) factory.host(port) else factory.join(f.ip.trim(), port)
            session.start(role, link)
        }
    }

    /** Partenaire simulé : liaison en mémoire, aucun réseau (le vrai partira au GameScreen). */
    private fun startSimulated() {
        val role = form.value.role
        if (role == null) {
            form.update { it.copy(error = LobbyError.RoleNotChosen) }
            return
        }
        viewModelScope.launch {
            val pair = InMemoryLinkPair()
            session.start(role, if (role == Role.CHARACTER) pair.a else pair.b)
        }
    }

    private data class FormState(val role: Role? = null, val ip: String = "", val port: String = "", val error: LobbyError? = null)
}
