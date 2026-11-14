package com.example.app.game.input

import com.example.app.game.contract.InputSource
import com.example.app.game.contract.Inputs
import com.example.app.game.contract.Role
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

/**
 * [InputSource] en mémoire, implémentation de départ de la feature 00-contrat :
 * renvoie un [Inputs] neutre. A remplacer par l'InputMapper (capteurs + entrées,
 * feature 03-capteurs, propriétaire B).
 */
class InMemoryInputSource : InputSource {

    override fun inputs(role: Role): Flow<Inputs> = flowOf(Inputs())
}
