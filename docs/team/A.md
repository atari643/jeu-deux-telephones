# Fiche du développeur A (à lire par l'IA de A au début de CHAQUE session)

Tu es l'IA du développeur **A** (outil : opencode + Qwen3.8-27B local, agent `dev`). Tu codes **le jeu pur** :
moteur (physique, collisions, rotation du monde), niveaux, rendu Canvas. Le développeur B (autre IA) code les
capteurs, la liaison réseau, l'InputMapper et le lobby. Vous ne touchez jamais aux fichiers l'un de l'autre.

## Tes fichiers (et seulement eux)
| Dossier (`app/src/main/kotlin/com/example/app/...` + même chemin sous `src/test/`) | Contenu |
|---|---|
| `game/engine/` | `GameEngine`, `GameConfig`, collisions, `levels/` |
| `ui/game/` | `GameUiState`, `GameViewModel` (boucle à pas fixe), `GameScreen` (Canvas) |
| `app/src/main/assets/levels/` | niveaux en texte |
| `docs/features/01-*`, `04-*`, `05-*` | tes specs et tâches |
Partagés (tu AJOUTES tes lignes, tu ne réécris pas celles de B) : `di/AppModule.kt`, `ui/navigation/`,
`res/values/strings.xml` (préfixe `game_` / `level_`), `ScreenCatalog.kt`, `src/test/.../testing/`.
**Pas à toi** : `data/sensors/`, `data/link/`, `game/input/`, `ui/lobby/` (B) ; `game/contract/`, `AGENTS.md`,
`tools/`, `.github/`, fichiers Gradle (commun, sauf pendant `commun/00-contrat`).

## Tes features, dans l'ordre
0. `commun/00-contrat` — `docs/features/00-contrat/` : le contrat + squelettes. B relit la PR.
1. `a/01-moteur` — `docs/features/01-moteur/`
2. `a/04-rendu` — `docs/features/04-rendu/`
3. `a/05-niveaux` — `docs/features/05-niveaux/`
4. `07-integration` avec B (branche `commun/07-integration`).

## Ne jamais attendre B
Tu codes contre le CONTRAT : le moteur reçoit des `Inputs` (B les produira via `InputMapper`) ; dans `GameViewModel`
utilise `GameLink` (tests : `InMemoryLinkPair`) et des `Inputs` construits à la main.

## Skills (dossier `.agents/skills/`) et commandes opencode
`game-engine` · `android-screen` · `gradle-build-fix`. Commandes : `/task <feature> <n>`, `/check`, `/fix`, `/see`,
`/review`, `/pr`. Une tâche = une session neuve (`/new`).

## Git (commandes exactes ; le push et la PR sont faits par l'humain)
```
git switch main && git pull
git switch -c a/01-moteur                        # ou: git switch a/01-moteur && git merge main
pwsh tools/check.ps1                             # CHECK OK obligatoire avant chaque commit
git add <tes fichiers> && git commit -m "feat(01-moteur): gravité et saut"
```
Hook `HORS PERIMETRE` ou `INTERDIT` : ne contourne pas ; annule, écris dans `docs/team/demandes.md`, préviens l'humain.

## Fin de session
Format de réponse de l'agent `dev` (`.opencode/agents/dev.md`), avec la branche en plus.
