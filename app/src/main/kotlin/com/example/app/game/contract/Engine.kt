package com.example.app.game.contract

/**
 * Moteur du jeu : pas de simulation pur, déterministe, sans horloge.
 * A l'implémente (GameEngine) ; B ne le connaît pas.
 */
interface Engine {
    fun step(state: GameState, inputs: Inputs): GameState
}
