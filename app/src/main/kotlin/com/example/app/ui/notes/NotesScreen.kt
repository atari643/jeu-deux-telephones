package com.example.app.ui.notes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.app.R
import com.example.app.domain.model.Note
import com.example.app.ui.theme.AppTheme
import org.koin.androidx.compose.koinViewModel

/** testTags de l'ecran : utilises par les tests ET par les dumps UI (tools/see.ps1). */
object NotesTags {
    const val INPUT = "notes_input"
    const val ADD = "notes_add"
    const val LIST = "notes_list"
    const val EMPTY = "notes_empty"
    const val LOADING = "notes_loading"
    const val ERROR = "notes_error"
    const val INFO = "notes_info"

    fun item(id: Long) = "notes_item_$id"

    fun delete(id: Long) = "notes_delete_$id"
}

/** Point d'entree avec etat : recupere le ViewModel (Koin) et delegue a l'ecran sans etat. */
@Composable
fun NotesRoute(
    onNavigateToAbout: () -> Unit = {},
    viewModel: NotesViewModel = koinViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    NotesScreen(state = state, onEvent = viewModel::onEvent, onNavigateToAbout = onNavigateToAbout)
}

/** Ecran SANS etat : tout vient de [state], tout sort par [onEvent]. Testable et previewable. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesScreen(
    state: NotesUiState,
    onEvent: (NotesEvent) -> Unit,
    onNavigateToAbout: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.notes_title)) },
                actions = {
                    IconButton(onClick = onNavigateToAbout, modifier = Modifier.testTag(NotesTags.INFO)) {
                        Icon(Icons.Filled.Info, contentDescription = stringResource(R.string.about_title))
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            NoteInput(state = state, onEvent = onEvent)
            when {
                state.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(Modifier.testTag(NotesTags.LOADING))
                }

                state.notes.isEmpty() -> Text(
                    text = stringResource(R.string.notes_empty),
                    modifier = Modifier.testTag(NotesTags.EMPTY)
                )

                else -> NoteList(notes = state.notes, onDelete = { onEvent(NotesEvent.DeleteClicked(it)) })
            }
        }
    }
}

@Composable
private fun NoteInput(state: NotesUiState, onEvent: (NotesEvent) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(
            value = state.draft,
            onValueChange = { onEvent(NotesEvent.DraftChanged(it)) },
            label = { Text(stringResource(R.string.notes_input_label)) },
            isError = state.error != null,
            supportingText = state.error?.let { error ->
                { Text(text = error.message(), modifier = Modifier.testTag(NotesTags.ERROR)) }
            },
            singleLine = true,
            modifier = Modifier.weight(1f).testTag(NotesTags.INPUT)
        )
        Button(onClick = { onEvent(NotesEvent.AddClicked) }, modifier = Modifier.testTag(NotesTags.ADD)) {
            Text(stringResource(R.string.notes_add))
        }
    }
}

@Composable
private fun NoteList(notes: List<Note>, onDelete: (Long) -> Unit) {
    LazyColumn(modifier = Modifier.fillMaxWidth().testTag(NotesTags.LIST)) {
        items(notes, key = { it.id }) { note ->
            ListItem(
                headlineContent = { Text(note.title) },
                trailingContent = {
                    IconButton(onClick = { onDelete(note.id) }, modifier = Modifier.testTag(NotesTags.delete(note.id))) {
                        Icon(Icons.Filled.Delete, contentDescription = stringResource(R.string.notes_delete, note.title))
                    }
                },
                modifier = Modifier.testTag(NotesTags.item(note.id))
            )
        }
    }
}

@Composable
private fun NotesError.message(): String = when (this) {
    NotesError.BlankTitle -> stringResource(R.string.notes_error_blank)
}

/** Etats d'exemple partages par les @Preview et par ScreenCatalog (tests). */
object NotesPreviewData {
    val filled = NotesUiState(
        notes = listOf(Note(1, "Acheter du pain", 0), Note(2, "Appeler Marie", 0)),
        isLoading = false
    )
    val empty = NotesUiState(isLoading = false)
    val error = NotesUiState(isLoading = false, draft = " ", error = NotesError.BlankTitle)
    val loading = NotesUiState(isLoading = true)
}

@Preview(showBackground = true)
@Composable
private fun NotesScreenPreview() {
    AppTheme { NotesScreen(state = NotesPreviewData.filled, onEvent = {}) }
}

@Preview(showBackground = true)
@Composable
private fun NotesScreenEmptyPreview() {
    AppTheme { NotesScreen(state = NotesPreviewData.empty, onEvent = {}) }
}
