package com.example.app.ui.game

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * Etat pur (aucun moteur, aucune coroutine) : on verifie la forme de l'etat
 * par defaut, attendue par la spec 04-rendu (CA3/CA5). L'erreur NoSession
 * est derivee par le ViewModel (deja testé dans GameViewModelTest).
 */
class GameUiStateTest {

    @Test
    fun `when created with no arguments then state is completely empty`() {
        val state = GameUiState()

        assertNull(state.role)
        assertNull(state.linkState)
        assertNull(state.game)
        assertNull(state.liveWorldAngleDeg)
        assertNull(state.error)
    }

    @Test
    fun `when env role has a live rotation then state carries it separately from the snapshot`() {
        val state = GameUiState(liveWorldAngleDeg = 90.0)

        assertNotNull(state.liveWorldAngleDeg)
        assertEquals(90.0, state.liveWorldAngleDeg ?: 0.0, 1e-9)
        assertNull(state.game)
    }
}
