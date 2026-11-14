package com.example.app.data.sensors

import com.example.app.game.contract.SensorReading
import com.example.app.game.contract.SensorSource
import com.example.app.game.contract.SensorType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

/**
 * [SensorSource] en mémoire, implémentation de départ de la feature 00-contrat :
 * aucun capteur Android, aucun flux. A remplacer par AndroidSensorSource
 * (feature 03-capteurs, propriétaire B).
 */
class InMemorySensorSource : SensorSource {

    override fun availableTypes(): Set<SensorType> = emptySet()

    override fun readings(): Flow<SensorReading> = emptyFlow()
}
