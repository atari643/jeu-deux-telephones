# Tâches 01-moteur
Une tâche = 1 à 3 fichiers de prod + leurs tests, faisable dans une session neuve.
Cocher `[x]` seulement quand `pwsh tools/check.ps1` est vert. Chemins relatifs à `app/src/main/kotlin/com/example/app/`.

- [ ] 1. [A] Niveau et constantes — `game/engine/Level.kt`, `game/engine/GameConfig.kt` (compléter) ; test : `game/engine/TestLevels.kt` + `LevelTest`
- [ ] 2. [A] Gravité et sol — `game/engine/GameEngine.kt`, `game/engine/Collisions.kt` — test : `GameEngineGravityTest` (CA3 chute)
- [ ] 3. [A] Déplacement et murs — `GameEngine.kt`, `Collisions.kt` — test : `GameEngineMoveTest` (CA1)
- [ ] 4. [A] Saut — `GameEngine.kt` — test : `GameEngineJumpTest` (CA2)
- [ ] 5. [A] Rotation du monde — `game/engine/Gravity.kt`, `GameEngine.kt` — test : `GameEngineRotationTest` (CA4)
- [ ] 6. [A] Victoire / défaite — `GameEngine.kt` — test : `GameEngineStatusTest` (CA3 LOST, CA5)
- [ ] 7. [A] Déterminisme — test seul : `GameEngineDeterminismTest` (CA6)
