---
description: Relancer une tâche Gradle (défaut - compilation) et corriger la première erreur. Usage - /fix [tâche gradle]
agent: android-dev
---
Sortie Gradle :
!`pwsh tools/gradle.ps1 $ARGUMENTS`

Charge le skill `gradle-build-fix` et applique sa procédure (première erreur d'abord, 3 essais max).
