package com.example.app.ui.about

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.app.ui.theme.AppTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** Test UI sous Robolectric (JVM, sans emulateur) : on teste l'ecran SANS etat. */
@RunWith(AndroidJUnit4::class)
class AboutScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    private var backClicks = 0

    private fun show(state: AboutUiState) {
        composeRule.setContent { AppTheme { AboutScreen(state = state, onBackClick = { backClicks++ }) } }
    }

    @Test
    fun `when about screen shown then app name and version are displayed`() {
        show(AboutUiState(version = "1.0"))
        composeRule.onNodeWithTag(AboutTags.NAME).assertIsDisplayed().assertTextEquals("VibeApp")
        composeRule.onNodeWithTag(AboutTags.VERSION).assertIsDisplayed().assertTextEquals("Version 1.0")
    }

    @Test
    fun `when back clicked then onBackClick is called`() {
        show(AboutUiState(version = "1.0"))
        composeRule.onNodeWithTag(AboutTags.BACK).performClick()
        assertEquals(1, backClicks)
    }
}
