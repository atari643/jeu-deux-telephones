package com.example.app.ui.notes

import com.example.app.domain.model.Note
import com.example.app.testing.FakeNotesRepository
import com.example.app.testing.MainDispatcherRule
import com.example.app.testing.keepCollecting
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * Pattern : fake repository + keepCollecting(uiState) + onEvent(...) + assert sur uiState.value.
 * Le ViewModel est cree dans @Before (APRES MainDispatcherRule), jamais en initialiseur de champ.
 */
class NotesViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var repository: FakeNotesRepository
    private lateinit var viewModel: NotesViewModel

    @Before
    fun setUp() {
        repository = FakeNotesRepository(listOf(Note(1, "Existante", 0)))
        viewModel = NotesViewModel(repository)
    }

    @Test
    fun `when started then existing notes are shown`() = runTest {
        keepCollecting(viewModel.uiState)

        val state = viewModel.uiState.value
        assertEquals(listOf("Existante"), state.notes.map { it.title })
        assertFalse(state.isLoading)
    }

    @Test
    fun `when add clicked with text then note is added and draft cleared`() = runTest {
        keepCollecting(viewModel.uiState)

        viewModel.onEvent(NotesEvent.DraftChanged("Nouvelle"))
        viewModel.onEvent(NotesEvent.AddClicked)

        val state = viewModel.uiState.value
        assertEquals(listOf("Existante", "Nouvelle"), state.notes.map { it.title })
        assertEquals("", state.draft)
    }

    @Test
    fun `when add clicked with blank text then error is shown`() = runTest {
        keepCollecting(viewModel.uiState)

        viewModel.onEvent(NotesEvent.DraftChanged("   "))
        viewModel.onEvent(NotesEvent.AddClicked)

        assertEquals(NotesError.BlankTitle, viewModel.uiState.value.error)
        assertEquals(1, viewModel.uiState.value.notes.size)
    }

    @Test
    fun `when text changes after error then error is cleared`() = runTest {
        keepCollecting(viewModel.uiState)

        viewModel.onEvent(NotesEvent.AddClicked)
        viewModel.onEvent(NotesEvent.DraftChanged("a"))

        assertNull(viewModel.uiState.value.error)
    }

    @Test
    fun `when delete clicked then note is removed`() = runTest {
        keepCollecting(viewModel.uiState)

        viewModel.onEvent(NotesEvent.DeleteClicked(1))

        assertEquals(emptyList<Note>(), viewModel.uiState.value.notes)
    }
}
