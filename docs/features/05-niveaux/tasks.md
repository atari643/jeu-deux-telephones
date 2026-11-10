# Tâches 05-niveaux
Une tâche = 1 à 3 fichiers de prod + leurs tests, faisable dans une session neuve.
Cocher `[x]` seulement quand `pwsh tools/check.ps1` est vert. Chemins relatifs à `app/src/main/kotlin/com/example/app/`.

- [ ] 1. [A] Parseur — `game/engine/LevelParser.kt` — test : `LevelParserTest` (CA1)
- [ ] 2. [A] Trois niveaux — `app/src/main/assets/levels/level1.txt`, `level2.txt`, `level3.txt` — test : `LevelAssetsTest` (CA2)
- [ ] 3. [A] Niveaux faisables — test seul : `LevelSolvableTest` (CA3)
- [ ] 4. [A] Progression — `game/engine/LevelCatalog.kt`, `ui/game/GameViewModel.kt` — test : `GameViewModelLevelsTest` (CA4)
