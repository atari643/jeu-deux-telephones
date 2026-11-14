package com.example.app.testing

import com.example.app.game.contract.Inputs
import com.example.app.game.contract.InputSource
import com.example.app.game.contract.Role
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * [InputSource] en mémoire pour les tests : on pousse des [Inputs] avec [emit]
 * et on les lit sur [inputs]. Le [Role] est ignoré : une seule source de flux.
 */
class FakeInputSource : InputSource {

    private val flow = MutableSharedFlow<Inputs>(extraBufferCapacity = Int.MAX_VALUE)

    override fun inputs(role: Role): Flow<Inputs> = flow.asSharedFlow()

    suspend fun emit(inputs: Inputs) {
        flow.emit(inputs)
    }
}
