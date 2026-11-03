# 00-contrat - Contrat commun et squelette du jeu

Propriétaire : A (B relit la PR)  ·  Branche : `feat/00-contrat`  ·  Décisions : 0001 à 0004

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

## Contenu du contrat (`game/contract/`)
- `PlayerState(x, y, vx, vy, onGround)`, `GameState(tick, player, worldAngleDeg, levelId, status)`,
  `GameStatus { WAITING, RUNNING, WON, LOST }`, `Inputs(moveX, jump, worldAngleDeg, darkWorld)`.
- `NetMessage` scellé : `Hello(role)`, `Ready`, `CharInput(seq, moveX, jump)`,
  `EnvInput(seq, angleDeg, special)`, `Snapshot(state)`, `Ping(id)`, `Pong(id)`. `Role { CHARACTER, ENVIRONMENT }`.
- `interface GameLink { val incoming: Flow<NetMessage>; suspend fun send(m: NetMessage); suspend fun close() }`
- `interface SensorSource { fun readings(): Flow<SensorReading> }`, `SensorReading(type, values: List<Float>, timestampNs)`,
  `SensorType { GAME_ROTATION, ACCELEROMETER, GYROSCOPE, PROXIMITY, LIGHT }`.
- `interface Engine { fun step(state: GameState, inputs: Inputs): GameState }`.

## Hors périmètre
Physique réelle, sockets TCP, capteurs Android, niveaux : features suivantes.
