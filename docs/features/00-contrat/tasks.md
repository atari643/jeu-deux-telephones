# Tâches 00-contrat
Une tâche = 1 à 3 fichiers de prod + leurs tests, faisable dans une session neuve.
`[A]`/`[B]` = qui la fait. Cocher `[x]` seulement quand `pwsh tools/check.ps1` est vert.

- [ ] 1. [A] Types d'état — `game/contract/GameState.kt` (PlayerState, GameState, GameStatus), `game/contract/Inputs.kt` — test : `GameStateTest` (CA1)
- [ ] 2. [A] Messages — `game/contract/NetMessage.kt` (+ Role), `game/contract/NetCodec.kt` — test : `NetCodecTest` aller-retour de chaque message (CA2)
- [ ] 3. [A] Interfaces — `game/contract/GameLink.kt`, `game/contract/SensorSource.kt` (+ SensorReading, SensorType), `game/contract/Engine.kt` — compile seulement
- [ ] 4. [A] Liaison mémoire — `data/link/InMemoryLinkPair.kt` — test : `InMemoryLinkPairTest` avec Turbine (CA3)
- [ ] 5. [A] Moteur stub + config — `game/engine/GameEngine.kt`, `game/engine/GameConfig.kt` ; fake de test `testing/FakeSensorSource.kt` — test : `GameEngineTest` (CA4)
- [ ] 6. [A] Écran Lobby — `ui/lobby/LobbyUiState.kt`, `LobbyViewModel.kt`, `LobbyScreen.kt` — tests : `LobbyViewModelTest`, `LobbyScreenTest` (CA5)
- [ ] 7. [A] Écran Game — `ui/game/GameUiState.kt`, `GameViewModel.kt` (dépend de `Engine` + `GameLink`), `GameScreen.kt` (Canvas) — tests : `GameViewModelTest`, `GameScreenTest` (CA6)
- [ ] 8. [A] Câblage — `di/AppModule.kt`, `ui/navigation/AppNavHost.kt` (Lobby au départ), `strings.xml`, `ScreenCatalog.kt` — `pwsh tools/see.ps1 LobbyScreen` et `GameScreen` (CA5, CA6, CA7)
- [ ] 9. [B] Relire la PR `feat/00-contrat` avec son IA (contrat clair ? manque-t-il un message ?) et lancer `pwsh tools/check.ps1` sur sa machine
