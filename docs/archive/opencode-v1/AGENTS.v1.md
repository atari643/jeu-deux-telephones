# Projet : app Android Kotlin (vibe coding avec modèle local)

Tu développes UNE app Android native. Rien d'autre (pas de web, pas de backend, pas de KMP).
Ton contexte est petit (32k) : lis peu, écris peu, une seule chose à la fois.

## Stack FIGÉE (ne jamais en sortir)
- Kotlin 2.4, AGP 9 (Kotlin intégré : PAS de plugin `kotlin-android`), un seul module `:app`.
- UI : Jetpack Compose + Material3. JAMAIS de XML layout, Fragment, LiveData, RxJava, Hilt/Dagger.
- Architecture : MVVM. `XxxViewModel` + `StateFlow<XxxUiState>` + `sealed interface XxxEvent`.
- DI : Koin (`di/AppModule.kt`). Données : Room (local), Ktor (réseau). Navigation Compose typée.
- Tests : JUnit4, Robolectric + Compose UI test, Roborazzi, fakes (MockK seulement si inévitable).
- Versions : `gradle/libs.versions.toml`. Ne JAMAIS modifier une version ni ajouter une dépendance.
  Besoin d'une nouvelle lib -> STOP, demande à l'humain.

## Structure (package `com.example.app`, sources dans `app/src/main/kotlin/`)
```
ui/<feature>/<Feature>Screen.kt     Route (avec ViewModel) + Screen (SANS état) + Tags + PreviewData + @Preview
ui/<feature>/<Feature>UiState.kt    UiState (data class) + Error (enum) + Event (sealed interface)
ui/<feature>/<Feature>ViewModel.kt  logique de l'écran
ui/navigation/AppNavHost.kt         destinations @Serializable
domain/model/                       modèles métier purs (data class)
data/local/  data/remote/  data/repository/   Room, Ktor, interfaces Repository + impl
di/AppModule.kt                     UNE ligne par classe injectée
```
MODÈLE À COPIER : la feature `ui/notes` (+ ses tests dans `app/src/test/kotlin/.../ui/notes`).
Avant d'écrire un fichier, ouvre son équivalent dans `notes` et reproduis la même forme.

## Outils (toujours via ces scripts, jamais `gradlew` directement)
| Besoin | Commande |
|---|---|
| Compiler | `pwsh tools/gradle.ps1 :app:compileDebugKotlin` |
| Tests | `pwsh tools/gradle.ps1 :app:testDebugUnitTest` (filtre : `--tests "*NomTest*"`) |
| Formater (auto) | `pwsh tools/gradle.ps1 :app:spotlessApply` |
| Qualité | `pwsh tools/gradle.ps1 :app:detekt` |
| VOIR un écran | `pwsh tools/see.ps1 <NomEcran>` (arbre UI texte, sans émulateur) |
| Régression visuelle | `pwsh tools/gradle.ps1 :app:verifyRoborazziDebug` |
| Doc Android officielle | `pwsh tools/docs.ps1 search "<mots>"` puis `pwsh tools/docs.ps1 code <url>` |
| Appareil réel | `pwsh tools/device.ps1 status|run|ui|logs` |

## Règles de travail
1. Une tâche = 1 à 3 fichiers. Lis le fichier AVANT de l'éditer. Édite par petits blocs.
2. Après CHAQUE modification de code : compiler. Erreur -> skill `gradle-build-fix`, 3 essais max,
   puis STOP et rapporte l'erreur exacte.
3. N'invente jamais une API. Doute -> cherche dans le code existant, puis `tools/docs.ps1`.
4. Tout composable interactif a un `Modifier.testTag(...)` défini dans l'objet `XxxTags`.
5. Textes affichés -> `res/values/strings.xml` + `stringResource(R.string.x)`. Jamais de texte en dur.
6. Pas de `!!`, pas de `GlobalScope`, pas de `runBlocking` hors tests, pas de `@Suppress`.
7. Ne supprime jamais un test pour le faire passer. Ne modifie pas `ScreenCatalogTest.kt` ni les snapshots.
8. Ne modifie JAMAIS `docs/PROGRESS.md` : seul `android-lead` le tient, via `pwsh tools/progress.ps1`.
9. Une action réussie ne se refait pas. Ne relance pas deux fois la même commande ou la même édition :
   si le résultat est OK, passe à l'étape suivante ou rends ton rapport.

## Definition of Done
`pwsh tools/gradle.ps1 :app:spotlessApply :app:detekt :app:testDebugUnitTest` = BUILD OK,
écran ajouté dans `ScreenCatalog.kt` et vérifié avec `tools/see.ps1`, critères de la spec cochés.
