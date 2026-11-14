package com.example.app.game.engine

import com.example.app.game.contract.Engine
import com.example.app.game.contract.GameState
import com.example.app.game.contract.Inputs

/**
 * Moteur du jeu (stub de la feature 00-contrat, CA4) : implémentation minimale
 * de [Engine] qui n'incrémente que [GameState.tick] et ne fait rien d'autre.
 * La physique réelle (gravité orientée, collisions, niveaux) arrive dans la
 * feature 01-moteur, toujours sur ce même pas fixe [GameConfig.STEP_SECONDS].
 */
class GameEngine : Engine {

    override fun step(state: GameState, inputs: Inputs): GameState = state.copy(tick = state.tick + 1)
}
