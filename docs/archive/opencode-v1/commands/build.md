---
description: Compiler l'app (debug) et corriger les erreurs éventuelles
agent: android-dev
subtask: true
---
Résultat de la compilation :
!`pwsh tools/gradle.ps1 :app:assembleDebug`

Si `BUILD OK` : réponds seulement `BUILD OK`.
Sinon : charge le skill `gradle-build-fix` et corrige (3 essais max), puis réponds avec ton format habituel.
