package com.example.app.ui.notes

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.app.ui.theme.AppTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** Test UI sous Robolectric (JVM, sans emulateur) : on teste l'ecran SANS etat. */
@RunWith(AndroidJUnit4::class)
class NotesScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    private val events = mutableListOf<NotesEvent>()

    private fun show(state: NotesUiState) {
        composeRule.setContent { AppTheme { NotesScreen(state = state, onEvent = { events += it }) } }
    }

    @Test
    fun `when notes exist then each note is displayed`() {
        show(NotesPreviewData.filled)
        composeRule.onNodeWithTag(NotesTags.item(1)).assertIsDisplayed()
        composeRule.onNodeWithTag(NotesTags.item(2)).assertIsDisplayed()
    }

    @Test
    fun `when no notes then empty message is displayed`() {
        show(NotesPreviewData.empty)
        composeRule.onNodeWithTag(NotesTags.EMPTY).assertIsDisplayed()
    }

    @Test
    fun `when error then error message is displayed`() {
        show(NotesPreviewData.error)
        composeRule.onNodeWithTag(NotesTags.ERROR, useUnmergedTree = true).assertIsDisplayed()
    }

    @Test
    fun `when typing and clicking add then events are sent`() {
        show(NotesPreviewData.empty)
        composeRule.onNodeWithTag(NotesTags.INPUT).performTextInput("Pain")
        composeRule.onNodeWithTag(NotesTags.ADD).performClick()
        assertEquals(listOf(NotesEvent.DraftChanged("Pain"), NotesEvent.AddClicked), events)
    }

    @Test
    fun `when delete clicked then delete event is sent`() {
        show(NotesPreviewData.filled)
        composeRule.onNodeWithTag(NotesTags.delete(2)).performClick()
        assertEquals(listOf(NotesEvent.DeleteClicked(2)), events)
    }
}
