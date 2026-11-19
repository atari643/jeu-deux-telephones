# 04-rendu - Écran de jeu : boucle, réseau côté jeu, dessin

Propriétaire : **A**  ·  Branche : `a/04-rendu`  ·  Décisions : 0002, 0004  ·  Skill : `android-screen`
Prérequis : `01-moteur` fusionnée.

## Objectif
`GameViewModel` fait tourner le jeu à pas fixe et échange avec l'autre téléphone via `GameSession.link` ;
`GameScreen` dessine le niveau, le joueur et la rotation du monde sur un Canvas.

## Critères d'acceptation
- CA1 : sur `GameEvent.Frame(nanos)`, le ViewModel appelle `engine.step` exactement `floor(temps écoulé / STEP)` fois (fake Engine qui compte).
- CA2 : rôle CHARACTER (hôte) : les Inputs viennent de `InputSource.inputs(CHARACTER)` + le dernier `EnvInput.angleDeg` reçu ;
  un `Snapshot` est envoyé tous les 2 pas.
- CA3 : rôle ENVIRONMENT : l'état affiché = dernier `Snapshot` reçu ; envoie `EnvInput` (~30/s) à partir de
  `InputSource.inputs(ENVIRONMENT)` ; sa propre rotation est affichée tout de suite (sans attendre le snapshot).
- CA4 : `GameScreen` dessine tuiles + joueur ; snapshots Roborazzi `GameScreen_default` et `GameScreen_rotated` (90°).
- CA5 : `WON` / `LOST` -> panneau avec texte et bouton « Rejouer » (testTags) ; `DISCONNECTED` -> message + retour au lobby.
- CA6 : simulateur sur PC (sans téléphone ni émulateur) : une `@Preview` `GamePlaygroundPreview` jouable dans le mode
  interactif des aperçus Compose d'Android Studio. Elle fait tourner le VRAI `GameEngine` à pas fixe sur le niveau de test,
  dessine avec le même code que `GameScreen` et affiche des boutons ◀ ▶ Saut ⟲ ⟳ (rotation ±15°) qui remplacent les
  capteurs. La logique (état, pas, inputs) est dans une classe `GamePlayground` pur Kotlin testée ; le composable ne fait
  qu'afficher et transmettre les clics. Aucune dépendance ajoutée (`ui-tooling` est déjà là).

## Hors périmètre
Capteurs et sockets réels (B) : utiliser `FakeInputSource`, `InMemoryLinkPair`, `InMemoryGameSession`.
