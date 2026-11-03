---
name: android-screen
description: Recette pour un écran Compose du projet (Route/Screen sans état, ViewModel StateFlow, Koin, navigation typée, strings, testTag, ScreenCatalog) et pour l'écran de jeu (Canvas + boucle withFrameNanos). À charger pour toute tâche dans ui/ ou di/.
---

# Écran Compose

## Fichiers (copie `ui/notes/` : NotesUiState.kt, NotesViewModel.kt, NotesScreen.kt)
1. `ui/<f>/<F>UiState.kt` : `data class <F>UiState`, `enum class <F>Error`, `sealed interface <F>Event`.
2. `ui/<f>/<F>ViewModel.kt` : dépendances = interfaces ; `val uiState: StateFlow<...>` ;
   UNE fonction publique `fun onEvent(event: <F>Event)` avec `when` exhaustif ; `viewModelScope.launch`.
3. `ui/<f>/<F>Screen.kt`, dans cet ordre : `object <F>Tags`, `<F>Route(viewModel = koinViewModel())`,
   `<F>Screen(state, onEvent, modifier)` SANS état, `object <F>PreviewData`, `@Preview`.
4. `di/AppModule.kt` : `viewModelOf(::<F>ViewModel)` ; impl -> interface : `singleOf(::Impl) { bind<Interface>() }`.
5. `ui/navigation/AppNavHost.kt` : `@Serializable data object <F>Destination` + `composable<<F>Destination> { <F>Route() }`.
   La Route reçoit des lambdas (`onBack: () -> Unit`), jamais le NavController.
6. `res/values/strings.xml` : chaque texte affiché ; `stringResource(R.string.x)`.
7. `app/src/test/kotlin/.../ui/catalog/ScreenCatalog.kt` : `"<F>Screen_default" to { <F>Screen(<F>PreviewData.default, onEvent = {}) },`
   puis `pwsh tools/see.ps1 <F>Screen`.

Tests : copier `ui/notes/NotesViewModelTest.kt` (`MainDispatcherRule`, `keepCollecting`, fakes de `testing/`)
et `NotesScreenTest.kt` (état en entrée, événements en sortie, `onNodeWithTag`).

## Écran de jeu (Canvas)
```kotlin
@Composable
fun GameScreen(state: GameUiState, onEvent: (GameEvent) -> Unit, modifier: Modifier = Modifier) {
    Canvas(modifier.fillMaxSize().testTag(GameTags.CANVAS)) {
        // dessiner state.game : drawRect / drawCircle ; rotation du monde : rotate(state.worldAngleDeg) { ... }
    }
}
// Dans la Route uniquement : la boucle d'images
LaunchedEffect(Unit) { while (isActive) { withFrameNanos { now -> viewModel.onEvent(GameEvent.Frame(now)) } } }
```
- Le ViewModel accumule le temps et appelle `engine.step` à pas fixe (1/60 s) ; l'écran ne fait que dessiner.
- Aucune physique, aucun capteur, aucun socket dans un composable.
- Test : `GameScreen` avec un `GameUiState` figé (snapshot Roborazzi), et `GameViewModelTest` avec
  `FakeSensorSource` + `InMemoryLinkPair`.

## Règles
Tout élément cliquable a `Modifier.testTag(<F>Tags.X)`. Icônes : seulement `Icons.Filled.*` du jeu
"core" (Add, Close, Check, Settings, PlayArrow, Refresh, Info, Warning, Home, Person, Star...). Doute -> un `Text`.
Cibles tactiles >= 48dp.
Experimental Material3 : `@OptIn(ExperimentalMaterial3Api::class)` sur la fonction.
