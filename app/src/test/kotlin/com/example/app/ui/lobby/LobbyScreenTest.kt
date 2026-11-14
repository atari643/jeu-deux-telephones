package com.example.app.ui.lobby

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.app.game.contract.Role
import com.example.app.ui.theme.AppTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** Test UI sous Robolectric (JVM, sans emulateur) : on teste l'ecran SANS etat. */
@RunWith(AndroidJUnit4::class)
class LobbyScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    private val events = mutableListOf<LobbyEvent>()

    private fun show(state: LobbyUiState) {
        composeRule.setContent { AppTheme { LobbyScreen(state = state, onEvent = { events += it }) } }
    }

    @Test
    fun `when default state then all interactive elements are displayed`() {
        show(LobbyPreviewData.default)

        composeRule.onNodeWithTag(LobbyTags.ROLE_CHARACTER).assertIsDisplayed()
        composeRule.onNodeWithTag(LobbyTags.ROLE_ENVIRONMENT).assertIsDisplayed()
        composeRule.onNodeWithTag(LobbyTags.IP).assertIsDisplayed()
        composeRule.onNodeWithTag(LobbyTags.PORT).assertIsDisplayed()
        composeRule.onNodeWithTag(LobbyTags.START).assertIsDisplayed()
        composeRule.onNodeWithTag(LobbyTags.SIMULATED).assertIsDisplayed()
    }

    @Test
    fun `when role chips clicked then role events are sent`() {
        show(LobbyPreviewData.default)

        composeRule.onNodeWithTag(LobbyTags.ROLE_CHARACTER).performClick()
        composeRule.onNodeWithTag(LobbyTags.ROLE_ENVIRONMENT).performClick()

        assertEquals(
            listOf<LobbyEvent>(
                LobbyEvent.RoleSelected(Role.CHARACTER),
                LobbyEvent.RoleSelected(Role.ENVIRONMENT)
            ),
            events
        )
    }

    @Test
    fun `when ip and port typed then change events are sent`() {
        show(LobbyPreviewData.default)

        composeRule.onNodeWithTag(LobbyTags.IP).performTextInput("192.168.1.10")
        composeRule.onNodeWithTag(LobbyTags.PORT).performTextInput("4242")

        // L'ecran est sans etat : le 2e input recompose le 1er champ et emet un reset vide ;
        // on verifie donc l'event attendu du 1er champ ET le dernier event recu.
        assertTrue(events.contains(LobbyEvent.IpChanged("192.168.1.10")))
        assertEquals(LobbyEvent.PortChanged("4242"), events.last())
    }

    @Test
    fun `when start clicked then start event is sent`() {
        show(LobbyPreviewData.default)

        composeRule.onNodeWithTag(LobbyTags.START).performClick()

        assertEquals(listOf<LobbyEvent>(LobbyEvent.StartClicked), events)
    }

    @Test
    fun `when simulated partner clicked then simulated event is sent`() {
        show(LobbyPreviewData.default)

        composeRule.onNodeWithTag(LobbyTags.SIMULATED).performClick()

        assertEquals(listOf<LobbyEvent>(LobbyEvent.SimulatedPartnerClicked), events)
    }

    @Test
    fun `when error then error message is displayed`() {
        show(LobbyPreviewData.error)

        composeRule.onNodeWithTag(LobbyTags.ERROR).assertIsDisplayed()
    }
}
