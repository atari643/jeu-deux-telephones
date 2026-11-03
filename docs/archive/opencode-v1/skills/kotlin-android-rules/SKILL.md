---
name: kotlin-android-rules
description: Règles impératives du projet Android Kotlin (stack figée, interdits, style Kotlin, coroutines, gestion d'erreurs, que faire en cas de doute sur une API). À charger avant d'écrire ou de modifier du code Kotlin.
---

# Règles Kotlin / Android du projet

## Stack autorisée (rien d'autre)
Compose Material3 · ViewModel + StateFlow · Koin · Room · Ktor + kotlinx.serialization ·
Navigation Compose typée · coroutines/Flow · JUnit4 · Robolectric · Roborazzi · MockK · Turbine.
Toute autre bibliothèque = INTERDITE (demander à l'humain). Ne touche pas aux fichiers `*.gradle.kts`.

## Interdits absolus
| Interdit | À la place |
|---|---|
| XML layout, `Fragment`, `findViewById` | Composable |
| `LiveData`, `observe` | `StateFlow` + `collectAsStateWithLifecycle()` |
| `GlobalScope`, `runBlocking` (hors tests) | `viewModelScope.launch` |
| `!!` | `?.`, `?:`, `requireNotNull(x) { "msg" }` |
| `lateinit var` dans le code de prod | `val` initialisée, ou nullable |
| `Context` dans un ViewModel | enum d'erreur + `stringResource` dans l'écran |
| Texte en dur dans l'UI | `stringResource(R.string.xxx)` |
| `var` publique mutable dans un ViewModel | `private val _x = MutableStateFlow`, exposer `StateFlow` |
| `@Suppress(...)`, désactiver une règle detekt | corriger le code |
| `Thread.sleep`, `delay` pour "attendre" | flux / états |
| Imports `*` | imports explicites |

## Style
- `val` par défaut. Classes de données immuables (`data class` + `copy`).
- Fonctions ≤ 40 lignes, ≤ 5 paramètres (sauf composables). Un fichier = une responsabilité.
- Noms : `XxxScreen`, `XxxViewModel`, `XxxUiState`, `XxxEvent`, `XxxRepository`, `RoomXxxRepository`,
  `FakeXxxRepository`, tags `"feature_element"` en snake_case.
- `when` sur sealed/enum : exhaustif, SANS branche `else`.
- Constantes : `private const val STOP_TIMEOUT_MS = 5_000L` en haut du fichier (pas de nombre magique).

## Coroutines / Flow
- Repository : `fun observeX(): Flow<List<X>>` (lecture continue), `suspend fun addX(...)` (écriture).
- ViewModel : `combine(...)`/`map` + `stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), Initial)`.
- Mise à jour d'état : `_state.update { it.copy(...) }` (jamais `_state.value = _state.value.copy(...)`).

## Erreurs
- Réseau/IO : `try { ... } catch (e: IOException) { _state.update { it.copy(error = XError.Network) } }`.
  Ne jamais attraper `Exception` ou `Throwable` (casse l'annulation des coroutines).
- Erreur affichée = valeur d'enum dans l'UiState, traduite dans l'écran.

## Doute sur une API ? (OBLIGATOIRE avant d'inventer)
1. Chercher un usage existant dans `app/src` (outil grep).
2. `pwsh tools/docs.ps1 search "<classe ou concept>"` puis `pwsh tools/docs.ps1 code <url>`.
3. Toujours rien -> écrire la version la plus simple avec des API déjà utilisées dans le projet,
   et le signaler dans le rapport (`DOUTE: ...`).

## Imports fréquents (copier tels quels)
```kotlin
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.androidx.compose.koinViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import com.example.app.R
```
