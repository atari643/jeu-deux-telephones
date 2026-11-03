---
name: android-testing
description: Comment écrire et lancer les tests du projet - tests de ViewModel (fake + MainDispatcherRule), tests d'écran Compose sous Robolectric, test Koin, snapshots Roborazzi. À charger pour écrire, corriger ou lancer des tests.
---

# Tests (tous sur la JVM : `app/src/test/kotlin/com/example/app/...`)

## Outils existants (réutiliser, ne pas recréer)
- `testing/MainDispatcherRule.kt` — obligatoire dans tout test de ViewModel.
- `testing/FlowTestUtils.kt` — `keepCollecting(flow)`.
- `testing/FakeNotesRepository.kt` — modèle de fake (MutableStateFlow en mémoire).

## Test de ViewModel (modèle : `ui/notes/NotesViewModelTest.kt`)
```kotlin
class ProfileViewModelTest {
    @get:Rule val mainDispatcherRule = MainDispatcherRule()
    private lateinit var repository: FakeProfileRepository
    private lateinit var viewModel: ProfileViewModel

    @Before fun setUp() {                       // créer le ViewModel ICI, jamais en initialiseur de champ
        repository = FakeProfileRepository()
        viewModel = ProfileViewModel(repository)
    }

    @Test fun `when save clicked with blank name then error is shown`() = runTest {
        keepCollecting(viewModel.uiState)
        viewModel.onEvent(ProfileEvent.NameChanged(" "))
        viewModel.onEvent(ProfileEvent.SaveClicked)
        assertEquals(ProfileError.BlankName, viewModel.uiState.value.error)
    }
}
```
- Un test = un comportement. Nom : `` `when <action> then <résultat>` ``.
- Fake > mock. MockK seulement pour une classe sans interface : `mockk<X> { coEvery { f() } returns y }`.
- Lire l'état avec `viewModel.uiState.value` APRÈS `keepCollecting`. Pas de `delay`, pas de `Thread.sleep`.

## Test d'écran (modèle : `ui/notes/NotesScreenTest.kt`)
```kotlin
@RunWith(AndroidJUnit4::class)
class ProfileScreenTest {
    @get:Rule val composeRule = createComposeRule()     // import androidx.compose.ui.test.junit4.v2.createComposeRule
    private val events = mutableListOf<ProfileEvent>()
    private fun show(state: ProfileUiState) =
        composeRule.setContent { AppTheme { ProfileScreen(state = state, onEvent = { events += it }) } }

    @Test fun `when save clicked then event is sent`() {
        show(ProfilePreviewData.default)
        composeRule.onNodeWithTag(ProfileTags.SAVE).performClick()
        assertEquals(listOf(ProfileEvent.SaveClicked), events)
    }
}
```
- On teste l'écran SANS état : un état en entrée, des événements en sortie.
- Trouver : `onNodeWithTag(TAG)`, `onNodeWithText("…")`. Texte dans un enfant fusionné : `useUnmergedTree = true`.
- Agir : `performClick()`, `performTextInput("x")`, `performScrollToNode(hasTestTag(T))`.
- Vérifier : `assertIsDisplayed()`, `assertIsEnabled()`, `assertIsNotEnabled()`, `assertTextContains("x")`,
  `assertDoesNotExist()`, `assertIsOn()`.

## Koin
`di/AppModuleTest.kt` vérifie tout `appModule`. Ne pas le modifier sauf pour ajouter un type
Android/librairie dans `extraTypes` (ex. `SavedStateHandle::class`).

## Snapshots Roborazzi (images comparées automatiquement)
- Ajouter l'écran dans `ui/catalog/ScreenCatalog.kt` (une entrée par état).
- Nouvel écran ou changement VOULU : `pwsh tools/gradle.ps1 :app:recordRoborazziDebug`.
- Vérifier qu'on n'a rien cassé : `pwsh tools/gradle.ps1 :app:verifyRoborazziDebug`.
- Ne JAMAIS ré-enregistrer pour masquer une différence non voulue.

## Lancer
```
pwsh tools/gradle.ps1 :app:testDebugUnitTest --tests "*ProfileViewModelTest*"   # un fichier
pwsh tools/gradle.ps1 :app:testDebugUnitTest                                   # tout
```
Test rouge : lire le message ET la ligne `a Fichier.kt:N`. Corriger le CODE si le test décrit le bon
comportement ; corriger le test seulement s'il est faux par rapport à la spec.
