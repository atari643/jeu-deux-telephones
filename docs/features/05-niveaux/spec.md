# 05-niveaux - Niveaux en fichiers texte et progression

Propriétaire : **A**  ·  Branche : `a/05-niveaux`  ·  Skill : `game-engine`
Prérequis : `01-moteur` fusionnée (04 conseillée).

## Objectif
Les niveaux sont des fichiers texte faciles à modifier ; 3 niveaux qui OBLIGENT à coopérer (un ravin
infranchissable sans rotation du monde) ; à la victoire on passe au niveau suivant.

## Format (`app/src/main/assets/levels/level1.txt`)
Une ligne = une rangée : `#` mur, `.` vide, `S` départ, `E` sortie. Toutes les lignes ont la même longueur.

## Critères d'acceptation
- CA1 : `LevelParser.parse(texte)` -> `Level` ; caractère inconnu ou lignes de longueurs différentes -> erreur explicite (ligne, colonne).
- CA2 : les 3 fichiers de `assets/levels/` se parsent sans erreur (test JVM qui lit `src/main/assets`).
- CA3 : chaque niveau est faisable : une suite d'Inputs écrite dans le test mène à `WON` ; sans rotation (angle 0) le niveau 2 est perdu.
- CA4 : après `WON`, l'événement « Suivant » charge le niveau suivant ; après le dernier, écran de fin.

## Hors périmètre
Éditeur de niveaux, sauvegarde de progression (Room) : non.
