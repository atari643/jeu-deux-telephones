---
description: Lancer les tests JVM (optionnel - filtre, ex. /test NotesViewModelTest) et analyser les échecs
agent: android-tester
subtask: true
---
Résultat des tests (filtre : "$ARGUMENTS") :
!`pwsh tools/gradle.ps1 :app:testDebugUnitTest --tests "*$ARGUMENTS*"`

Si `BUILD OK` : réponds seulement `TESTS OK`.
Sinon : pour chaque test rouge, dis si c'est le TEST qui est faux (corrige-le) ou le CODE (STATUT: BUG, ne corrige pas).
