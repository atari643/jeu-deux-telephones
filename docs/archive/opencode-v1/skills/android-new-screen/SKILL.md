---
name: android-new-screen
description: Recette pas à pas pour AJOUTER un nouvel écran ou une nouvelle feature Android (UiState, ViewModel, Screen, navigation, Koin, catalogue, tests). À charger dès qu'on crée un écran, une page, une vue ou une feature.
---

# Ajouter un écran `<Feature>` (ex. `Settings`)

Fais les étapes DANS L'ORDRE. Compile après les étapes 2, 4, 6 et 8.
Pour chaque fichier, ouvre d'abord son équivalent dans `ui/notes/` et copie sa forme.

## 1. Modèle (si nouvelles données) — `domain/model/<Thing>.kt`
```kotlin
data class Thing(val id: Long, val name: String)
```

## 2. Repository (si données) — `data/repository/<Thing>Repository.kt`
Interface + implémentation dans le même fichier (voir `NotesRepository.kt`).
Room : `@Entity` + `@Dao` dans `data/local/`, ajouter l'entité dans `AppDatabase.entities`
et incrémenter `version`. Fake de test : `app/src/test/kotlin/com/example/app/testing/Fake<Thing>Repository.kt`.

## 3. État + événements — `ui/<feature>/<Feature>UiState.kt`
```kotlin
data class SettingsUiState(
    val isLoading: Boolean = true,
    val darkMode: Boolean = false,
    val error: SettingsError? = null
)
enum class SettingsError { SaveFailed }
sealed interface SettingsEvent {
    data class DarkModeChanged(val enabled: Boolean) : SettingsEvent
    data object SaveClicked : SettingsEvent
}
```

## 4. ViewModel — `ui/<feature>/<Feature>ViewModel.kt`
- Constructeur = dépendances (interfaces). Jamais de `Context`.
- `val uiState: StateFlow<XUiState>` via `stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), XUiState())`
  ou `MutableStateFlow` privé + `asStateFlow()` si pas de flux de données.
- UNE fonction publique : `fun onEvent(event: XEvent)` avec un `when` exhaustif.
- Appels suspend dans `viewModelScope.launch { }`.

## 5. Écran — `ui/<feature>/<Feature>Screen.kt` (skill `compose-ui` pour les détails)
Dans cet ordre : `object XTags`, `@Composable fun XRoute(viewModel: XViewModel = koinViewModel())`,
`@Composable fun XScreen(state, onEvent, modifier)`, composables privés, `object XPreviewData`, `@Preview`.

## 6. Koin — `di/AppModule.kt`
```kotlin
viewModelOf(::SettingsViewModel)
singleOf(::RoomThingRepository) { bind<ThingRepository>() }
```
Imports : `org.koin.core.module.dsl.viewModelOf`, `singleOf`, `bind`.

## 7. Navigation — `ui/navigation/AppNavHost.kt`
```kotlin
@Serializable data object SettingsDestination                 // sans argument
@Serializable data class DetailDestination(val id: Long)      // avec argument

composable<SettingsDestination> { SettingsRoute() }
composable<DetailDestination> { entry -> DetailRoute(id = entry.toRoute<DetailDestination>().id) }
// naviguer : navController.navigate(DetailDestination(id = 3))   import androidx.navigation.toRoute
```
Passe des lambdas `onOpenDetail: (Long) -> Unit` à la Route, pas le `navController`.

## 8. Textes — `app/src/main/res/values/strings.xml`
Une `<string name="settings_title">…</string>` par texte affiché. Apostrophe : `\'`.

## 9. Catalogue + tests
- `app/src/test/kotlin/com/example/app/ui/catalog/ScreenCatalog.kt` : ajouter
  `"SettingsScreen_default" to { SettingsScreen(state = SettingsPreviewData.default, onEvent = {}) },`
- Tests : skill `android-testing` (ViewModelTest + ScreenTest).
- Puis `pwsh tools/see.ps1 SettingsScreen` et `pwsh tools/gradle.ps1 :app:spotlessApply :app:detekt :app:testDebugUnitTest`.

## Ne PAS faire
- Créer un nouveau module Gradle, un nouveau fichier de build, un `Fragment`, une `Activity` de plus.
- Passer le ViewModel ou le NavController à `XScreen`.
- Mettre de la logique (if métier, appels repository) dans un composable.
