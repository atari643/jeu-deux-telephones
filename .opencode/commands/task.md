---
description: Réaliser UNE tâche d'une feature (test d'abord, code, check). Usage - /task 00-contrat 3
agent: dev
---
Feature : $1 — tâche numéro : $2

Liste des tâches :
!`Get-Content docs/features/$1/tasks.md`

Réalise UNIQUEMENT la tâche demandée en suivant ton cycle (test d'abord, code, `pwsh tools/check.ps1`),
coche-la dans tasks.md, puis réponds avec ton format final. Tâche marquée pour l'autre développeur ([B] si
tu es A) -> STOP et dis-le.
