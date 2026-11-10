# 00-contrat - Contrat commun et squelette du jeu

Propriétaire : A (B relit la PR)  ·  Branche : `commun/00-contrat`  ·  Décisions : 0001 à 0004

## Objectif
Poser tout ce que les deux développeurs partagent AVANT de travailler en parallèle : types du jeu,
messages réseau, interfaces + fakes, et squelettes des écrans/DI/navigation. Après cette feature, A et B
ne modifient plus les mêmes fichiers.

## Critères d'acceptation
- CA1 : `GameState`, `PlayerState`, `Inputs` sont `@Serializable`, immuables, sans import Android.
- CA2 : chaque `NetMessage` encodé en une ligne JSON puis décodé redonne le même objet (`NetCodec`).
- CA3 : `InMemoryLinkPair` : un message envoyé par un bout est reçu par l'autre, dans l'ordre.
- CA4 : `GameEngine.step` stub incrémente `tick` et ne fait rien d'autre (moteur réel = feature 01).
- CA5 : écran `Lobby` : choisir le rôle (Personnage / Environnement), saisir IP:port, bouton
  « Partenaire simulé » ; chaque élément a un testTag ; visible avec `tools/see.ps1 LobbyScreen`.
- CA6 : écran `Game` : Canvas qui dessine le joueur (rectangle) à partir d'un `GameUiState` figé ;
  snapshot Roborazzi `GameScreen_default`.
- CA7 : `AppModule`, `AppNavHost` (Lobby = départ, puis Game), `strings.xml`, `ScreenCatalog` contiennent
  déjà les entrées de tous ces écrans ; `pwsh tools/check.ps1` = CHECK OK.

## Contenu du contrat (`game/contract/`, aucun import Android)
- `PlayerState(x, y, vx, vy, onGround)` (unités = tuiles, y vers le bas), `GameState(tick, player, worldAngleDeg,
  levelId, status)`, `GameStatus { WAITING, RUNNING, WON, LOST }`, `Inputs(moveX, jump, worldAngleDeg, darkWorld)`.
- `Role { CHARACTER, ENVIRONMENT }`. `NetMessage` scellé : `Hello(role)`, `Ready`, `CharInput(seq, moveX, jump)`,
  `EnvInput(seq, angleDeg, special)`, `Snapshot(state)`, `Ping(id)`, `Pong(id)`. `NetCodec` : une ligne JSON.
- `LinkState { CONNECTING, CONNECTED, DISCONNECTED }`.
  `interface GameLink { val state: StateFlow<LinkState>; val incoming: Flow<NetMessage>; suspend fun send(m: NetMessage); suspend fun close() }`
  `interface GameLinkFactory { suspend fun host(port: Int): GameLink; suspend fun join(ip: String, port: Int): GameLink }`
- `interface GameSession { val role: StateFlow<Role?>; val link: StateFlow<GameLink?>; fun start(role: Role, link: GameLink); suspend fun end() }`
  (le lobby de B la remplit, l'écran de jeu de A la lit).
- `SensorType { GAME_ROTATION, ACCELEROMETER, GYROSCOPE, PROXIMITY, LIGHT }`, `SensorReading(type, values: List<Float>, timestampNs)`,
  `interface SensorSource { fun availableTypes(): Set<SensorType>; fun readings(): Flow<SensorReading> }`.
- `interface InputSource { fun inputs(role: Role): Flow<Inputs> }` (B l'implémente avec capteurs + InputMapper ; A la consomme).
- `interface Engine { fun step(state: GameState, inputs: Inputs): GameState }`.

## Implémentations de départ (remplacées ensuite par chaque propriétaire)
- `data/link/InMemoryLinkPair.kt` (deux `GameLink` reliés par des `Channel`), `data/link/InMemoryGameSession.kt`.
- `game/engine/GameEngine.kt` stub + `GameConfig.kt`.
- Tests (`testing/`) : `FakeSensorSource`, `FakeInputSource`, `FakeGameLinkFactory`.

## Hors périmètre
Physique réelle, sockets TCP, capteurs Android, niveaux : features suivantes.
