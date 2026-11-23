package com.example.app.ui.game

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.example.app.R
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@RunWith(RobolectricTestRunner::class)
class GameScreenTest {

    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `GameScreen displays the game canvas when session connected`() {
        rule.setContent {
            GameScreen(GamePreviewData.default, onEvent = {})
        }
        rule.onNodeWithTag(GameTags.CANVAS).assertIsDisplayed()
    }

    @Test
    fun `GameScreen displays the game canvas when session not connected`() {
        rule.setContent {
            GameScreen(GameUiState(), onEvent = {})
        }
        rule.onNodeWithTag(GameTags.CANVAS).assertIsDisplayed()
    }

    @Test
    fun `GameScreen with rotated state draws the level at 90 degrees`() {
        val state = GamePreviewData.rotated
        rule.setContent {
            GameScreen(state, onEvent = {})
        }
        rule.onNodeWithTag(GameTags.CANVAS).assertIsDisplayed()
        // Le rendu 90 deg est verifie par le snapshot Roborazzi GameScreen_rotated (CA4).
        assertNull(state.liveWorldAngleDeg)
        assertEquals(90.0, state.game?.worldAngleDeg ?: -1.0, 0.001)
    }

    @Test
    fun `GameScreen shows no panel when game is running and link connected`() {
        rule.setContent {
            GameScreen(GamePreviewData.default, onEvent = {})
        }
        rule.onNodeWithTag(GameTags.REPLAY).assertDoesNotExist()
        rule.onNodeWithTag(GameTags.RESULT).assertDoesNotExist()
        rule.onNodeWithTag(GameTags.DISCONNECTED).assertDoesNotExist()
        rule.onNodeWithTag(GameTags.EXIT).assertDoesNotExist()
    }

    @Test
    fun `GameScreen shows replay panel when game is won`() {
        var event: GameEvent? = null
        rule.setContent {
            GameScreen(GamePreviewData.won, onEvent = { event = it })
        }
        rule.onNodeWithTag(GameTags.RESULT)
            .assertTextEquals(RuntimeEnvironment.getApplication().getString(R.string.game_won))
        rule.onNodeWithTag(GameTags.REPLAY).assertIsDisplayed()
        rule.onNodeWithTag(GameTags.DISCONNECTED).assertDoesNotExist()

        rule.onNodeWithTag(GameTags.REPLAY).performClick()
        assertEquals(GameEvent.Replay, event)
    }

    @Test
    fun `GameScreen shows replay panel when game is lost`() {
        var event: GameEvent? = null
        rule.setContent {
            GameScreen(GamePreviewData.lost, onEvent = { event = it })
        }
        rule.onNodeWithTag(GameTags.RESULT)
            .assertTextEquals(RuntimeEnvironment.getApplication().getString(R.string.game_lost))
        rule.onNodeWithTag(GameTags.REPLAY).assertIsDisplayed()

        rule.onNodeWithTag(GameTags.REPLAY).performClick()
        assertEquals(GameEvent.Replay, event)
    }

    @Test
    fun `GameScreen shows disconnected panel with back button when link disconnected`() {
        var exited = false
        rule.setContent {
            GameScreen(GamePreviewData.disconnected, onEvent = {}, onExit = { exited = true })
        }
        rule.onNodeWithTag(GameTags.DISCONNECTED)
            .assertTextEquals(RuntimeEnvironment.getApplication().getString(R.string.game_disconnected))
        rule.onNodeWithTag(GameTags.EXIT).assertIsDisplayed()
        rule.onNodeWithTag(GameTags.REPLAY).assertDoesNotExist()

        rule.onNodeWithTag(GameTags.EXIT).performClick()
        assertTrue(exited)
    }
}
