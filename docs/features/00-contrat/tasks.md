# Tâches 00-contrat
Branche : `commun/00-contrat` (faite par A, relue par B). Une tâche = 1 à 3 fichiers de prod + leurs tests.
Cocher `[x]` seulement quand `pwsh tools/check.ps1` est vert.

- [x] 1. [A] Types d'état — `game/contract/GameState.kt` (PlayerState, GameState, GameStatus), `game/contract/Inputs.kt` — test : `GameStateTest` (CA1)
- [x] 2. [A] Messages — `game/contract/NetMessage.kt` (+ Role), `game/contract/NetCodec.kt` — test : `NetCodecTest` aller-retour de chaque message (CA2)
- [x] 3. [A] Interfaces réseau — `game/contract/GameLink.kt` (GameLink, LinkState, GameLinkFactory), `game/contract/GameSession.kt` — compile
- [x] 4. [A] Interfaces capteurs/moteur — `game/contract/Sensors.kt` (SensorType, SensorReading, SensorSource, InputSource), `game/contract/Engine.kt` — compile
- [ ] 5. [A] Liaison mémoire — `data/link/InMemoryLinkPair.kt`, `data/link/InMemoryGameSession.kt` — test : `InMemoryLinkPairTest` avec Turbine (CA3)
- [ ] 6. [A] Moteur stub + fakes — `game/engine/GameEngine.kt`, `game/engine/GameConfig.kt` ; tests : `testing/FakeSensorSource.kt`, `FakeInputSource.kt`, `FakeGameLinkFactory.kt`, `GameEngineTest` (CA4)
- [ ] 7. [A] Écran Lobby (squelette) — `ui/lobby/LobbyUiState.kt`, `LobbyViewModel.kt`, `LobbyScreen.kt` — tests : `LobbyViewModelTest`, `LobbyScreenTest` (CA5)
- [ ] 8. [A] Écran Game (squelette) — `ui/game/GameUiState.kt`, `GameViewModel.kt` (Engine + GameSession), `GameScreen.kt` (Canvas) — tests : `GameViewModelTest`, `GameScreenTest` (CA6)
- [ ] 9. [A] Câblage — `di/AppModule.kt`, `ui/navigation/AppNavHost.kt` (Lobby au départ), `strings.xml`, `ScreenCatalog.kt` — `pwsh tools/see.ps1 LobbyScreen` et `GameScreen` (CA5-CA7)
- [ ] 10. [B] Relire la PR `commun/00-contrat` avec son IA (contrat clair ? message ou champ manquant pour 02/03/06 ?) et approuver
