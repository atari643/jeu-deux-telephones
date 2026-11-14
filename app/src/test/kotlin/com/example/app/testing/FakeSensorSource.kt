package com.example.app.testing

import com.example.app.game.contract.SensorReading
import com.example.app.game.contract.SensorSource
import com.example.app.game.contract.SensorType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * [SensorSource] en mémoire pour les tests : on pousse des lectures avec [emit]
 * et on les lit sur [readings]. Aucun capteur Android, aucun temps réel.
 */
class FakeSensorSource(
    private val types: Set<SensorType> = SensorType.entries.toSet()
) : SensorSource {

    private val flow = MutableSharedFlow<SensorReading>(extraBufferCapacity = Int.MAX_VALUE)

    override fun availableTypes(): Set<SensorType> = types

    override fun readings(): Flow<SensorReading> = flow.asSharedFlow()

    suspend fun emit(reading: SensorReading) {
        flow.emit(reading)
    }

    companion object {
        fun reading(type: SensorType, vararg values: Float, timestampNs: Long = 0L): SensorReading =
            SensorReading(type = type, values = values.toList(), timestampNs = timestampNs)
    }
}
