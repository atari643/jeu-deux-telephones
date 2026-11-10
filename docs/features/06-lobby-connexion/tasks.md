# Tâches 06-lobby-connexion
Une tâche = 1 à 3 fichiers de prod + leurs tests, faisable dans une session neuve.
Cocher `[x]` seulement quand `pwsh tools/check.ps1` est vert. Chemins relatifs à `app/src/main/kotlin/com/example/app/`.

- [ ] 1. [B] État et rôle — `ui/lobby/LobbyUiState.kt`, `ui/lobby/LobbyViewModel.kt` — test : `LobbyViewModelTest` (CA1, validation CA3)
- [ ] 2. [B] Héberger / rejoindre — `LobbyViewModel.kt` — test : `LobbyViewModelConnectTest` avec `FakeGameLinkFactory` (CA2, CA3)
- [ ] 3. [B] Capteurs disponibles — `LobbyViewModel.kt`, `LobbyUiState.kt` — test : avec `FakeSensorSource` (CA4)
- [ ] 4. [B] Écran — `ui/lobby/LobbyScreen.kt`, `strings.xml`, `ScreenCatalog.kt` (3 entrées) — test : `LobbyScreenTest` + snapshots (CA1-CA6)
- [ ] 5. [B] Navigation vers Game — `ui/navigation/AppNavHost.kt` — test : `LobbyScreenTest` (lambda `onStartGame` appelée)
