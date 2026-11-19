package com.example.app.game.engine

import com.example.app.game.contract.NetMessage
import kotlin.math.PI

/**
 * Constantes du moteur (jamais de magic number dans [GameEngine]).
 * Le moteur est un pas fixe à [TICK_RATE] pas par seconde, indépendant du
 * temps réel. Toutes les vitesses et accélérations sont exprimées en
 * tuiles / seconde (ou tuiles / seconde²).
 */
object GameConfig {

    /** Nombre de pas de simulation par seconde. */
    const val TICK_RATE: Int = 60

    /** Durée d'un pas de simulation en secondes (1/60, jamais le temps réel). */
    const val STEP_SECONDS: Double = 1.0 / 60.0

    /** Conversion de degrés en radians (rotation du monde, CA4). */
    const val DEG_TO_RAD: Double = PI / 180.0

    /** Accélération gravitaire vers le bas (tuiles/s²) quand le monde n'est pas tourné. */
    const val GRAVITY: Double = 24.0

    /** Vitesse de course horizontale à `moveX = ±1` (tuiles/s). */
    const val RUN_SPEED: Double = 6.0

    /** Vitesse initiale vers le haut d'un saut au sol (tuiles/s). */
    const val JUMP_SPEED: Double = 12.0

    /** Vitesse de chute maximale (tuiles/s), pour rester borné et déterministe. */
    const val MAX_FALL_SPEED: Double = 20.0

    /** Identifiant du niveau de départ. */
    const val START_LEVEL_ID: String = "01-flat"

    /**
     * Frequence a laquelle l'hote (role CHARACTER) envoie un [NetMessage.Snapshot]
     * au telephone environnement (04-rendu CA2) : un snapshot tous les N pas.
     */
    const val SNAPSHOT_EVERY_N_STEPS: Long = 2L

    /**
     * Frequence a laquelle l'environnement (role ENVIRONMENT) envoie un
     * [NetMessage.EnvInput] a l'hote (04-rendu CA3) : un EnvInput tous les N pas,
     * soit ~30 envois/s a [TICK_RATE] = 60 pas/s.
     */
    const val ENV_INPUT_EVERY_N_STEPS: Long = 2L
}
