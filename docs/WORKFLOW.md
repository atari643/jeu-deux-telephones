# Workflow IA de l'équipe

Deux développeurs, deux IA différentes (A : opencode + Qwen3.8-27B local ; B : au choix), un dépôt GitHub.
Principe : **peu d'instructions, beaucoup de vérifications automatiques.** Ce que les IA doivent respecter
est dans des fichiers partagés ; ce qui décide si le travail est bon, c'est la machine (tests, CI), pas l'IA.

## 1. Les 5 éléments du workflow
| Élément | Fichier | Rôle |
|---|---|---|
| Règles communes | `AGENTS.md` (~70 lignes, écrit et relu par nous) | Stack, structure, commandes, cycle, interdits. Lu nativement par opencode, Codex, Cursor, Copilot, Gemini dans Android Studio ; `.gemini/settings.json` pour Gemini CLI. |
| Décisions | `docs/decisions/NNNN-*.md` | Pourquoi TCP, pourquoi A fait autorité, quels capteurs, pourquoi Canvas. Évite qu'une IA « réinvente » un choix. |
| Spec + tâches | `docs/features/<NN-nom>/spec.md` + `tasks.md` | Critères d'acceptation testables ; tâches de 1-3 fichiers marquées `[A]`/`[B]`. |
| Vérification locale | `pwsh tools/check.ps1` | Formatage, detekt, tests JVM, régression visuelle, APK. « CHECK OK » = fini. |
| Arbitre | `.github/workflows/ci.yml` + PR | Rien n'entre dans `main` sans CI vert et relecture de l'autre développeur. |

## 2. Le cycle (inspiré de superpowers / agilité, version allégée)
```
spec (humain + IA)  ->  tasks.md  ->  pour chaque tâche, SESSION NEUVE :
      test qui échoue -> code minimal -> tools/check.ps1 -> cocher -> commit
->  PR  ->  relecture par l'autre (avec SON IA)  ->  CI vert  ->  merge
```
- **Session neuve par tâche** : l'IA repart d'un contexte propre ; l'état vit dans les fichiers et git.
- **Test d'abord** : le test est l'oracle ; une IA ne peut pas « croire » que c'est fini.
- **Relecture croisée** : deux IA différentes ne font pas les mêmes erreurs.

## 3. Travailler à deux sans conflits
1. **Répartition par couches, pas par téléphone** (sinon les deux toucheraient au moteur, au réseau et au rendu) :
   - **A** (opencode + Qwen local) = le jeu pur : moteur, niveaux, rendu (`game/engine`, `ui/game`).
     Kotlin pur et déterministe, idéal pour un modèle local au contexte limité.
   - **B** (Gemini dans Android Studio) = le matériel : capteurs, liaison TCP, entrées, lobby
     (`data/sensors`, `data/link`, `game/input`, `ui/lobby`). Demande plus de connaissances Android et des tests sur téléphone.
   - Fiches de rôle lues par chaque IA : `docs/team/A.md`, `docs/team/B.md`. Propriété des fichiers : `.github/ownership.txt`.
2. **Le contrat d'abord** (`commun/00-contrat`, fait par A, relu par B) : types, messages, interfaces
   (`GameLink`, `GameSession`, `SensorSource`, `InputSource`, `Engine`), fakes, et squelettes des fichiers partagés.
   Ensuite chacun code contre les fakes de l'autre et n'attend jamais.
3. **Phases** : 0 = contrat (A) + prise en main (B) ; 1 = 01-moteur (A) // 02-liaison-tcp, 03-capteurs (B) ;
   2 = 04-rendu, 05-niveaux (A) // 06-lobby-connexion (B) ; 3 = 07-intégration ensemble sur deux téléphones.
4. **Protections automatiques** (une IA qui ignore ses consignes est quand même arrêtée) :
   | Niveau | Quoi | Bloque |
   |---|---|---|
   | Consignes | `AGENTS.md` + fiche de rôle | (l'IA sait ce qu'elle doit faire) |
   | Hooks git (`tools/setup-team.ps1`) | `.githooks/pre-commit`, `pre-push` -> `tools/scope-check.sh` | commit sur `main`, branche ou fichiers de l'autre |
   | GitHub | ruleset sur `main` + CI (`check`, `scope`) | push direct, PR sans approbation de l'autre, CI rouge |
5. **Branches** : `a/<NN-feature>`, `b/<NN-feature>`, `commun/<sujet>` (contrat, outils, règles : approuvé par les deux).
   Une PR par feature, fusion en « Squash and merge ». Besoin chez l'autre -> `docs/team/demandes.md`.

## 4. Ce qu'on a volontairement écarté (et pourquoi)
| Outil / idée | Verdict | Raison |
|---|---|---|
| superpowers (obra) | idées gardées, pas installé | ~22k tokens au démarrage : impossible dans la fenêtre du modèle local ; on garde spec -> plan -> TDD -> revue. |
| spec-kit, BMAD, Kiro | écartés | Très lourds ; un benchmark (fournisseur) montre que la spec n'aide pas sur les corrections de bugs. |
| tdd-guard | écarté | Ne supporte pas Kotlin/Gradle ; le CI joue ce rôle. |
| Beaucoup d'agents et de skills | réduit à 2 agents + 3 skills (côté opencode) | Pas de preuve de gain ; chaque sous-agent recharge tout le contexte sur un GPU unique. |
| AGENTS.md généré par IA (`/init`) | interdit | Les fichiers de contexte générés font *baisser* la réussite ; écrits à la main : léger gain. |
| RAG vectoriel sur le code | écarté | Projet petit : `rg`, les modèles à copier et la doc Android hors ligne (`tools/docs.ps1`) suffisent. |

## 5. Ce qui est partagé et ce qui est propre à chaque IA
- **Partagé** : `AGENTS.md`, `docs/`, `tools/`, et les skills `.agents/skills/` (standard Agent Skills, lu par
  opencode ET par Gemini dans Android Studio) : `game-engine` (A), `android-hardware` (B), `android-screen`, `gradle-build-fix`.
- **A (opencode)** : `.opencode/` = agents `dev` + `reviewer`, commandes `/task`, `/check`, `/fix`, `/see`, `/review`,
  `/pr`, `/docs`. Ancien workflow (6 agents, 9 skills) archivé dans `docs/archive/opencode-v1/`.
- **B (Gemini, Android Studio)** : rien de spécifique ; guide humain `docs/team/B-DEMARRAGE.md` (installation,
  prompt de début de session, problèmes fréquents).

## Sources
- Gloaguen et al., *Evaluating AGENTS.md*, 2026 — https://arxiv.org/abs/2602.11988
- SkillsBench, 2026 — https://arxiv.org/html/2602.12670v1
- SWE-agent (interface + garde-fous de lint), 2024 — https://arxiv.org/pdf/2405.15793
- METR, productivité des développeurs avec IA, 2025 — https://arxiv.org/abs/2507.09089
- Chroma, *Context Rot* — https://research.trychroma.com/context-rot
- Fowler, *Harness engineering* — https://www.martinfowler.com/articles/harness-engineering.html
- Böckeler, outils de spec-driven development — https://www.martinfowler.com/articles/exploring-gen-ai/sdd-3-tools.html
- superpowers — https://github.com/obra/superpowers (coût en tokens : issue #190)
- Standard AGENTS.md — https://agents.md/
