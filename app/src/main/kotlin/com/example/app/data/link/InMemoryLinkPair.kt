package com.example.app.data.link

import com.example.app.game.contract.GameLink
import com.example.app.game.contract.LinkState
import com.example.app.game.contract.NetMessage
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow

/**
 * Paire de [GameLink] en mémoire, pour les tests et le partenaire simulé.
 * Un message envoyé par un bout est reçu par l'autre, dans l'ordre où il a
 * été envoyé (canal illimité, aucun réseau). [close] sur un bout déconnecte
 * les deux bouts et termine leurs `incoming`.
 */
class InMemoryLinkPair {

    /** Premier bout de la liaison. */
    val a: GameLink

    /** Deuxième bout de la liaison. */
    val b: GameLink

    init {
        val toA = Channel<NetMessage>(Channel.UNLIMITED)
        val toB = Channel<NetMessage>(Channel.UNLIMITED)
        val stateA = MutableStateFlow(LinkState.CONNECTED)
        val stateB = MutableStateFlow(LinkState.CONNECTED)
        a = InMemoryGameLink(inbound = toA, outbound = toB, stateFlow = stateA) {
            stateB.value = LinkState.DISCONNECTED
            toB.close()
        }
        b = InMemoryGameLink(inbound = toB, outbound = toA, stateFlow = stateB) {
            stateA.value = LinkState.DISCONNECTED
            toA.close()
        }
    }
}

private class InMemoryGameLink(
    private val inbound: Channel<NetMessage>,
    private val outbound: Channel<NetMessage>,
    private val stateFlow: MutableStateFlow<LinkState>,
    private val onClosed: () -> Unit
) : GameLink {

    override val state: StateFlow<LinkState> = stateFlow

    override val incoming: Flow<NetMessage> = inbound.receiveAsFlow()

    override suspend fun send(message: NetMessage) {
        check(stateFlow.value == LinkState.CONNECTED) {
            "send() sur une liaison fermée"
        }
        outbound.send(message)
    }

    override suspend fun close() {
        stateFlow.value = LinkState.DISCONNECTED
        onClosed()
        inbound.close()
    }
}
