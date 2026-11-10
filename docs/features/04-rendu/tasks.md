# Tâches 04-rendu
Une tâche = 1 à 3 fichiers de prod + leurs tests, faisable dans une session neuve.
Cocher `[x]` seulement quand `pwsh tools/check.ps1` est vert. Chemins relatifs à `app/src/main/kotlin/com/example/app/`.

- [ ] 1. [A] État et événements — `ui/game/GameUiState.kt` (compléter) — test : compile + `GameUiStateTest` si logique
- [ ] 2. [A] Boucle à pas fixe — `ui/game/GameViewModel.kt` — test : `GameViewModelLoopTest` (CA1)
- [ ] 3. [A] Rôle hôte — `GameViewModel.kt` — test : `GameViewModelHostTest` avec `InMemoryLinkPair` (CA2)
- [ ] 4. [A] Rôle environnement — `GameViewModel.kt` — test : `GameViewModelEnvTest` (CA3)
- [ ] 5. [A] Dessin Canvas — `ui/game/GameScreen.kt`, `ui/game/GameDrawing.kt` ; `ScreenCatalog.kt` (2 entrées) — test : `GameScreenTest` + snapshots (CA4)
- [ ] 6. [A] Fin de partie et déconnexion — `GameScreen.kt`, `GameViewModel.kt`, `strings.xml` — test : `GameScreenTest`, `GameViewModelTest` (CA5)
