package com.example.app.game.contract

import kotlinx.coroutines.flow.Flow

/**
 * Capteurs du téléphone environnement. Seuls B (AndroidSensorSource)
 * et les fakes les connaissent ; ils ne traversent jamais le réseau.
 */
enum class SensorType {
    GAME_ROTATION,
    ACCELEROMETER,
    GYROSCOPE,
    PROXIMITY,
    LIGHT
}

/**
 * Une lecture capteur : [type], les [values] brutes de l'événement
 * et [timestampNs] en nanosecondes (horloge système, uniquement côté B).
 */
data class SensorReading(val type: SensorType, val values: List<Float>, val timestampNs: Long)

/**
 * Flux de lectures capteurs. B l'implémente, B le teste ; A ne le connaît pas.
 */
interface SensorSource {
    fun availableTypes(): Set<SensorType>
    fun readings(): Flow<SensorReading>
}

/**
 * Entrées du jeu consommées par le moteur : B l'implémente
 * (capteurs + InputMapper), A la consomme via le ViewModel.
 */
interface InputSource {
    fun inputs(role: Role): Flow<Inputs>
}
