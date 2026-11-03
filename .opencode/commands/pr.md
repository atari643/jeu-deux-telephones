---
description: Préparer la description de la Pull Request de la branche courante dans build/pr.md (le push reste humain)
agent: dev
---
Branche et changements :
!`git status --short`
!`git log --oneline main..HEAD`

Écris `build/pr.md` en remplissant `.github/pull_request_template.md` (tâches de tasks.md couvertes,
comment c'est testé, IA utilisée : opencode + Qwen3.8-27B local). N'exécute ni push ni `gh`.
Réponds avec le titre de PR proposé et la commande que l'humain lancera :
`gh pr create --title "<titre>" --body-file build/pr.md`
