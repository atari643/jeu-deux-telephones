---
description: Vérification complète avant de déclarer terminé (formatage auto, detekt, tous les tests, régression visuelle)
agent: android-dev
subtask: true
---
Résultat de la vérification complète :
!`pwsh tools/gradle.ps1 :app:spotlessApply :app:detekt :app:verifyRoborazziDebug`

Si `BUILD OK` : réponds seulement `CHECK OK`.
Sinon : charge `gradle-build-fix`, corrige les problèmes un par un (3 essais max chacun) en relançant
la commande ciblée. ROBORAZZI différent : ne ré-enregistre (`:app:recordRoborazziDebug`) que si l'écran
concerné a été modifié volontairement par la tâche en cours ; sinon signale une régression.
