---
name: game-engine
description: Patterns du code de jeu SANS UI - moteur pur Kotlin (step à pas fixe, physique, niveaux), protocole réseau entre les 2 téléphones, capteurs et liaison derrière des interfaces avec fakes. À charger pour toute tâche dans game/, data/link/ ou data/sensors/.
---

# Code de jeu (pas d'UI)

## Où va quoi (package `com.example.app`)
| Dossier | Contenu | Android autorisé ? |
|---|---|---|
| `game/contract/` | `NetMessage`, `GameState`, `Inputs`, interfaces `GameLink`, `SensorSource`, `Engine` | NON |
| `game/engine/` | `GameEngine` (implémente `Engine`), collisions, `GameConfig`, niveaux | NON |
| `data/link/` | `TcpGameLink` (java.net), `InMemoryLinkPair` (tests + partenaire simulé) | oui (link) |
| `data/sensors/` | `AndroidSensorSource` (SensorManager), `FakeSensorSource` côté tests | oui (sensors) |
Ne copie PAS `ui/notes` pour ce code : ici pas de ViewModel, pas de Room, pas de Ktor.
Le contrat (`game/contract/`) ne change que si la tâche le dit (PR validée par les deux).

## Moteur : fonction pure à pas fixe
```kotlin
class GameEngine(private val level: Level) : Engine {
    override fun step(state: GameState, inputs: Inputs): GameState {
        val dt = GameConfig.STEP_SECONDS                 // 1/60 s, jamais le temps réel
        val vy = state.player.vy + GameConfig.GRAVITY * dt
        // ... calcul pur : aucune I/O, aucun random sans graine, aucune horloge
        return state.copy(tick = state.tick + 1, player = state.player.copy(vy = vy))
    }
}
```
- Toutes les constantes (gravité, vitesse, saut) dans `object GameConfig` (sinon detekt MagicNumber).
- `GameState` = `data class` immuable ; listes = `List`, jamais `MutableList` exposée.

## Test du moteur : JUnit pur, "après N pas"
```kotlin
class GameEngineTest {
    private val engine = GameEngine(TestLevels.flatWithRavine)
    private fun run(state: GameState, inputs: Inputs, steps: Int) =
        (1..steps).fold(state) { s, _ -> engine.step(s, inputs) }

    @Test fun `when jump pressed on ground then player goes up`() {
        val after = run(TestLevels.start, Inputs(jump = true), steps = 10)
        assertTrue(after.player.y < TestLevels.start.player.y)
    }
}
```
Pas de Robolectric, pas de coroutines ici : c'est rapide et déterministe.

## Protocole (téléphone A = hôte qui simule ; B envoie l'environnement)
```kotlin
@Serializable sealed interface NetMessage {
    @Serializable @SerialName("env") data class EnvInput(val seq: Int, val angleDeg: Float) : NetMessage
    @Serializable @SerialName("snap") data class Snapshot(val state: GameState) : NetMessage
}
```
Une ligne JSON par message : `Json.encodeToString(NetMessage.serializer(), msg)`.
Test : encoder puis décoder doit redonner le même objet.

## Interfaces + fakes (le code de jeu ne voit QUE les interfaces)
```kotlin
interface GameLink { val incoming: Flow<NetMessage>; suspend fun send(msg: NetMessage); suspend fun close() }
interface SensorSource { fun readings(): Flow<SensorReading> }
```
- Android : `callbackFlow { register...; awaitClose { unregister } }` dans `AndroidSensorSource`.
  Capteur absent (`getDefaultSensor` == null) -> flux vide + repli documenté dans la spec.
- Tests : `InMemoryLinkPair()` (deux bouts reliés par des `Channel`) et `FakeSensorSource`
  (`MutableSharedFlow`). Tester avec `runTest` + `turbine` (`flow.test { awaitItem() }`).
- Socket : `Dispatchers.IO`, `tcpNoDelay = true`, une coroutine lecture + une écriture, fermer dans `finally`.

## Interdits
Nouvelle dépendance (Nearby, libGDX...), `GlobalScope`, `Thread.sleep`, `System.currentTimeMillis()`
dans le moteur, `!!`, logique de jeu dans un composable.
