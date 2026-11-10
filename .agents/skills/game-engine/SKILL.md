---
name: game-engine
description: Moteur de jeu pur Kotlin du développeur A - step à pas fixe, physique, collisions, rotation du monde, niveaux, et ses tests JUnit "après N pas". À charger pour toute tâche dans game/engine/ ou sur les niveaux.
---

# Moteur de jeu (game/engine/, aucun import Android)

## Le contrat à respecter (game/contract/, FIGÉ)
`Engine.step(state: GameState, inputs: Inputs): GameState` · `GameState(tick, player, worldAngleDeg, levelId, status)`
· `PlayerState(x, y, vx, vy, onGround)` · `Inputs(moveX, jump, worldAngleDeg, darkWorld, special)`.
Les `Inputs` viennent de B (`game/input/InputMapper`) : le moteur ne connaît NI capteur NI réseau.
Besoin d'un champ en plus -> `docs/team/demandes.md`, pas de modification du contrat.

## Moteur : fonction pure à pas fixe
```kotlin
class GameEngine(private val level: Level) : Engine {
    override fun step(state: GameState, inputs: Inputs): GameState {
        val dt = GameConfig.STEP_SECONDS                 // 1/60 s, jamais le temps réel
        val vy = state.player.vy + GameConfig.GRAVITY * dt
        // calcul pur : aucune I/O, aucun random sans graine, aucune horloge
        return state.copy(tick = state.tick + 1, player = state.player.copy(vy = vy))
    }
}
```
- Constantes (gravité, vitesse, saut, taille des tuiles) dans `object GameConfig` (sinon detekt MagicNumber).
- `GameState` immuable ; listes en `List`, jamais `MutableList` exposée.
- Rotation du monde : la gravité suit `inputs.worldAngleDeg` (vecteur gravité tourné), le niveau ne bouge pas.
- Niveaux : `data class Level(tiles: List<String>, ...)`, construits dans `game/engine/levels/` ou lus depuis
  `app/src/main/assets/levels/*.txt` (une ligne = une rangée de tuiles : `#` mur, `.` vide, `S` départ, `E` sortie).

## Tests : JUnit pur, "après N pas" (pas de Robolectric, pas de coroutines)
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
Un test par critère d'acceptation. Niveaux de test minuscules (3-5 lignes) dans `TestLevels` (dossier de test du moteur).

## Interdits
Import `android.*` dans `game/`, `System.currentTimeMillis()`, `Random()` sans graine, `Thread.sleep`, `!!`,
dépendance nouvelle (libGDX...), logique de jeu dans un composable, modifier `game/contract/`.
