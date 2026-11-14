package com.example.app.game.engine

/**
 * Constantes du moteur (jamais de magic number dans [GameEngine]).
 * Le moteur est un pas fixe à [TICK_RATE] pas par seconde, indépendant du
 * temps réel ; la gravité, la vitesse et les niveaux arriveront avec la
 * feature 01-moteur.
 */
object GameConfig {

    /** Nombre de pas de simulation par seconde. */
    const val TICK_RATE: Int = 60

    /** Durée d'un pas de simulation en secondes (1/60, jamais le temps réel). */
    const val STEP_SECONDS: Double = 1.0 / 60.0

    /** Identifiant du niveau de départ. */
    const val START_LEVEL_ID: String = "01-flat"
}
