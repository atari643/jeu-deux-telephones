# Spec : About
Objectif : Afficher un écran « À propos » (nom de l'app + version 1.0), accessible via un bouton icône Info dans la top bar de l'écran des notes, avec un bouton retour vers les notes.
Hypothèses :
- La version « 1.0 » est une constante du ViewModel (`APP_VERSION = "1.0"`) : `build.gradle` est intouchable, pas de BuildConfig.
- Le nom de l'app réutilise la string existante `app_name` (« VibeApp »).
- Les strings `about_title`, `about_back`, `about_version` existent déjà dans `res/values/strings.xml` : aucune string à ajouter.
- Le retour = flèche arrière standard de la `TopAppBar` (`navController.popBackStack()`), pas un événement ViewModel.
- Écran 100 % statique : pas de chargement, pas d'erreur, pas de données persistées.
QUESTIONS : aucune

## Écrans
- AboutScreen : titre « À propos » (top bar avec icône retour), corps centré avec le nom de l'app (Text) et « Version 1.0 » (Text). Navigation : depuis NotesScreen via le clic sur l'icône Info de sa top bar.
- NotesScreen (modifié) : ajout d'un `IconButton` icône `Icons.Filled.Info` dans la top bar (paramètre `onNavigateToAbout: () -> Unit = {}`).

## État (AboutUiState)
| champ | type | défaut | rôle |
|---|---|---|---|
| version | String | "1.0" | version affichée via `about_version` |

## Événements (AboutEvent)
| événement | effet |
|---|---|
| aucun | écran statique ; le retour est géré par la navigation, pas par le ViewModel |

## Erreurs (AboutError)
| valeur | quand | texte affiché |
|---|---|---|
| aucune | — | — |

## Données
- Modèle : aucun (pas de data class métier)   - Source : aucune
- Repository : aucun

## testTags
| tag | élément |
|---|---|
| `notes_info` | bouton icône Info dans la top bar de NotesScreen (ajouté dans `NotesTags`) |
| `about_back` | bouton retour (icône) de la top bar d'AboutScreen |
| `about_app_name` | Text du nom de l'app |
| `about_version` | Text de la version |

## Critères d'acceptation (chacun vérifiable par test ou par tools/see.ps1)
- [ ] CA1 : Étant donné l'écran des notes, quand on clique sur le tag `notes_info`, alors l'écran About est affiché (titre « À propos »).
- [ ] CA2 : Étant donné l'écran About, quand on l'observe, alors le tag `about_app_name` affiche « VibeApp » et le tag `about_version` affiche « Version 1.0 ».
- [ ] CA3 : Étant donné l'écran About, quand on clique sur le tag `about_back`, alors l'écran des notes est affiché de nouveau.
- [ ] CA4 : `pwsh tools/see.ps1 AboutScreen_default` affiche les tags `about_app_name` et `about_version`.

## Tâches (ordre d'exécution, 1 à 3 fichiers chacune)
1. [dev] `ui/about/AboutUiState.kt` (UiState + Event vide) + `ui/about/AboutViewModel.kt` (constante `APP_VERSION = "1.0"`, `uiState` initialisé) + `di/AppModule.kt` (ligne `viewModelOf(::AboutViewModel)`)
2. [test] `app/src/test/kotlin/com/example/app/ui/about/AboutViewModelTest.kt` (état initial : version = "1.0")
3. [dev] `ui/about/AboutScreen.kt` (AboutRoute + AboutScreen sans état + AboutTags + AboutPreviewData + @Preview)
4. [dev] `ui/navigation/AppNavHost.kt` (`AboutDestination` + `composable<AboutDestination>`) + `ui/notes/NotesScreen.kt` (paramètre `onNavigateToAbout` + `NotesTags.INFO` + bouton Info) + `app/src/test/kotlin/com/example/app/ui/catalog/ScreenCatalog.kt` (entrée `AboutScreen_default`)
5. [test] `app/src/test/kotlin/com/example/app/ui/about/AboutScreenTest.kt` (CA2, CA4) + `recordRoborazziDebug`
6. [qa] vérification CA1/CA3 (navigation, test Compose) et CA4 avec `tools/see.ps1 AboutScreen_default`
