package com.example.app.ui.game

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class GameScreenTest {

    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `GameScreen displays the game canvas when session connected`() {
        rule.setContent {
            GameScreen(GamePreviewData.default)
        }
        rule.onNodeWithTag(GameTags.CANVAS).assertIsDisplayed()
    }

    @Test
    fun `GameScreen displays the game canvas when session not connected`() {
        rule.setContent {
            GameScreen(GameUiState())
        }
        rule.onNodeWithTag(GameTags.CANVAS).assertIsDisplayed()
    }

    @Test
    fun `GameScreen with rotated state draws the level at 90 degrees`() {
        val state = GamePreviewData.rotated
        rule.setContent {
            GameScreen(state)
        }
        rule.onNodeWithTag(GameTags.CANVAS).assertIsDisplayed()
        // Le rendu 90 deg est verifie par le snapshot Roborazzi GameScreen_rotated (CA4).
        assertNull(state.liveWorldAngleDeg)
        assertEquals(90.0, state.game?.worldAngleDeg ?: -1.0, 0.001)
    }
}
