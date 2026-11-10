# Projet : jeu de plateforme coopératif à 2 téléphones (Android, Kotlin)

Téléphone A = personnage (déplacements, sauts) et HÔTE qui simule le jeu. Téléphone B = environnement
(rotation du niveau). Liaison TCP sur le Wi-Fi local. 5 capteurs. Décisions : `docs/decisions/`.
Deux développeurs, chacun avec une IA différente : ce fichier et le CI sont les seules règles communes.

## Équipe : lis ceci AVANT toute action
1. Ton rôle : `git config team.role` (A ou B), sinon le préfixe de ta branche (`a/`, `b/`). Inconnu -> demande à l'humain.
2. Lis ta fiche : `docs/team/A.md` (moteur, niveaux, rendu) ou `docs/team/B.md` (capteurs, liaison, entrées, lobby).
3. Ne modifie QUE tes fichiers (`.github/ownership.txt`). Le hook git et le CI refusent le reste.
4. Branches : `a/<NN-feature>` ou `b/<NN-feature>` depuis `main` à jour. `commun/<sujet>` = travail commun
   (contrat, règles, outils) : tous les fichiers y sont autorisés, A comme B peuvent y committer, PR approuvée par les deux.
   Jamais de commit ni de push sur `main`, jamais `--no-verify`, jamais `push --force`, jamais la branche de l'autre.
5. Besoin d'un changement chez l'autre ou dans `game/contract/` : écris-le dans `docs/team/demandes.md`, puis STOP.
   C'est l'HUMAIN qui décide d'ouvrir une branche `commun/<sujet>`, jamais l'IA seule.
6. Avant de commencer : `git switch main && git pull`, puis crée ou reprends ta branche et `git merge main`.

## Stack FIGÉE
- Kotlin 2.4, AGP 9 (Kotlin intégré, PAS de plugin `kotlin-android`), un seul module `:app`, targetSdk 36.
- Compose + Material3 (jamais de XML layout/Fragment/LiveData/RxJava/Hilt), MVVM, Koin, kotlinx-serialization.
- Tests JVM : JUnit4, Robolectric, Compose UI test, Roborazzi, Turbine, fakes. Pas d'émulateur requis.
- Ne JAMAIS modifier `gradle/libs.versions.toml` ni ajouter de dépendance : demande à l'humain.

## Où va le code (package `com.example.app`, `app/src/main/kotlin/`)
```
game/contract/   messages réseau, GameState, Inputs, interfaces (GameLink, SensorSource, Engine). FIGÉ.
game/engine/     moteur pur Kotlin : step(state, inputs) à pas fixe, GameConfig, niveaux. AUCUN import Android.
game/input/      InputMapper : lectures capteurs -> Inputs. Pur Kotlin.
data/link/       TcpGameLink (java.net), InMemoryLinkPair (tests + partenaire simulé)
data/sensors/    AndroidSensorSource (callbackFlow autour de SensorManager)
ui/<ecran>/      <E>UiState.kt, <E>ViewModel.kt, <E>Screen.kt (modèle à copier : ui/notes/)
di/AppModule.kt  une ligne par classe injectée
```
`ui/notes` sert de MODÈLE de forme pour les écrans. Room et le client Ktor ne servent pas au jeu.

## Commandes (scripts filtrés ; jamais `gradlew` directement en local)
| Besoin | Commande |
|---|---|
| Tests ciblés | `pwsh tools/gradle.ps1 :app:testDebugUnitTest --tests "*NomTest*"` |
| Compiler | `pwsh tools/gradle.ps1 :app:compileDebugKotlin` |
| Tout vérifier (DoD) | `pwsh tools/check.ps1` |
| Voir un écran en texte | `pwsh tools/see.ps1 <NomEcran>` (après ajout dans `ScreenCatalog.kt`) |
| Doc Android hors ligne | `pwsh tools/docs.ps1 search "<mots>"` puis `pwsh tools/docs.ps1 code <url>` |
| Téléphone connecté | `pwsh tools/device.ps1 status|run|ui|logs` |

## Cycle d'une tâche (une session neuve par tâche)
1. Prendre UNE ligne de `docs/features/<f>/tasks.md` marquée pour toi (`[A]` ou `[B]`).
2. Lire la section utile de `docs/features/<f>/spec.md` et le fichier modèle à copier.
3. Écrire le test d'abord (il échoue), puis le code minimal. 1 à 3 fichiers de prod par tâche.
4. `pwsh tools/check.ps1` vert -> cocher la tâche, commit `<type>(<feature>): <résumé>`.
5. Fin de feature -> Pull Request ; l'AUTRE développeur relit avec son IA ; le CI doit être vert.

## Règles
1. Lis un fichier avant de l'éditer. N'invente pas d'API : cherche dans le code, puis `tools/docs.ps1`.
2. Moteur et contrat : pur Kotlin, déterministe (pas d'horloge, pas de random sans graine). Constantes dans un objet `XxxConfig` du dossier concerné (`GameConfig` pour A, `LinkConfig`/`InputConfig` pour B).
3. Capteurs et réseau : toujours derrière une interface, testés avec des fakes.
4. Composables : sans logique ; tout élément interactif a un `Modifier.testTag` défini dans `<E>Tags`.
5. Textes affichés dans `res/values/strings.xml` + `stringResource`. Jamais en dur.
6. Interdits : `!!`, `GlobalScope`, `runBlocking` hors tests, `@Suppress`, `Thread.sleep`.
7. Ne supprime jamais un test pour le faire passer. Ne modifie pas `ScreenCatalogTest.kt` ni les snapshots à la main.
8. `game/contract/` ne change que par une PR approuvée par les deux développeurs.
9. Erreur de build : corrige la PREMIÈRE erreur, 3 essais max, puis STOP et rapporte l'erreur exacte.
10. Une action réussie ne se refait pas.

## Definition of Done
`pwsh tools/check.ps1` = CHECK OK (spotless, detekt, tests, Roborazzi, APK), critères de la spec
couverts par des tests, écran ajouté dans `ScreenCatalog.kt` (si la tâche crée un écran), CI GitHub vert sur la PR.
