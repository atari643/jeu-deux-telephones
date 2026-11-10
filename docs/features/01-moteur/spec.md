# 01-moteur - Physique du personnage et rotation du monde

Propriétaire : **A**  ·  Branche : `a/01-moteur`  ·  Décisions : 0002  ·  Skill : `game-engine`
Prérequis : `00-contrat` fusionnée dans `main`.

## Objectif
Le moteur `GameEngine` fait marcher, sauter, tomber le personnage dans un niveau de tuiles, et la gravité suit
l'angle du monde (`Inputs.worldAngleDeg`) imposé par le téléphone B. Pur Kotlin, déterministe.

## Repères
Unités = tuiles, x vers la droite, y vers le bas. Pas fixe `GameConfig.STEP_SECONDS = 1/60`.
`Level(width, height, solid(x, y), start, exit)` est interne au moteur (pas dans le contrat).

## Critères d'acceptation
- CA1 : `moveX = 1` pendant 60 pas -> x augmente d'environ `GameConfig.RUN_SPEED` ; contre un mur, x ne dépasse pas le mur.
- CA2 : `jump = true` au sol -> y diminue puis le joueur retombe au sol ; `jump` en l'air est ignoré.
- CA3 : sans sol sous les pieds, le joueur tombe ; sorti du bas du niveau -> `status = LOST`.
- CA4 : `worldAngleDeg = 90` -> la gravité est tournée de 90° (le joueur « tombe » sur le côté).
- CA5 : toucher la tuile de sortie -> `status = WON` ; en `WON`/`LOST`, `step` ne change plus rien sauf `tick`.
- CA6 : même suite d'Inputs rejouée deux fois -> états strictement identiques.

## Hors périmètre
Lecture des niveaux depuis des fichiers (05), dessin (04), capteurs et réseau (B).
