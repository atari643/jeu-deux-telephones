---
description: Voir un écran en texte, sans émulateur (arbre UI). Usage - /see GameScreen
agent: dev
---
Arbre UI de "$ARGUMENTS" (rendu Robolectric, écran 411x891dp) :
!`pwsh tools/see.ps1 $ARGUMENTS`

Décris l'écran en 3-5 lignes (de haut en bas). Signale : élément interactif sans tag, texte coupé,
élément qui dépasse 411dp de large, cible tactile < 48dp. Sinon "rien".
