---
description: Voir un écran en texte, sans émulateur (arbre UI). Usage - /see NotesScreen
agent: android-qa
subtask: true
---
Arbre UI de "$ARGUMENTS" (rendu Robolectric, écran 411x891dp) :
!`pwsh tools/see.ps1 $ARGUMENTS`

Avec le skill `ui-verification` : décris l'écran en 3-5 lignes (de haut en bas), puis applique les
contrôles de mise en page et liste les problèmes trouvés (ou "rien").
