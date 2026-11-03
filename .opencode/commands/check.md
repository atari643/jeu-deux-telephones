---
description: Definition of Done - formatage auto, detekt, tous les tests, régression visuelle, APK
agent: dev
---
Résultat :
!`pwsh tools/check.ps1`

`CHECK OK` -> réponds seulement `CHECK OK`.
Sinon : skill `gradle-build-fix`, corrige les problèmes un par un (3 essais max chacun) avec la commande
ciblée, puis relance `pwsh tools/check.ps1` UNE fois. ROBORAZZI différent : ré-enregistre
(`pwsh tools/gradle.ps1 :app:recordRoborazziDebug`) seulement si la tâche a modifié cet écran exprès.
