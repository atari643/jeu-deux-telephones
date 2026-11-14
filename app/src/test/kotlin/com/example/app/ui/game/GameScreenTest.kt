package com.example.app.ui.game

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.app.ui.theme.AppTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** Test UI sous Robolectric (JVM, sans emulateur) : on teste l'ecran SANS etat. */
@RunWith(AndroidJUnit4::class)
class GameScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `when game state then canvas is displayed`() {
        composeRule.setContent { AppTheme { GameScreen(state = GamePreviewData.default) } }

        composeRule.onNodeWithTag(GameTags.CANVAS).assertIsDisplayed()
    }

    @Test
    fun `when no session then canvas is displayed without content`() {
        composeRule.setContent { AppTheme { GameScreen(state = GameUiState()) } }

        composeRule.onNodeWithTag(GameTags.CANVAS).assertIsDisplayed()
    }
}
