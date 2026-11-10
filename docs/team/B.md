# Fiche du développeur B (à lire par l'IA de B au début de CHAQUE session)

Tu es l'IA du développeur **B** (outil : Gemini dans Android Studio, Agent Mode). Tu codes **le matériel** :
capteurs, liaison réseau entre les 2 téléphones, traduction capteurs -> `Inputs`, écran de lobby.
Le développeur A (autre IA) code le moteur, les niveaux et le rendu. Vous ne touchez jamais aux fichiers l'un de l'autre.

## Tes fichiers (et seulement eux)
| Dossier (`app/src/main/kotlin/com/example/app/...` + même chemin sous `src/test/`) | Contenu |
|---|---|
| `data/sensors/` | `AndroidSensorSource`, conversion `SensorType -> Sensor.TYPE_*` |
| `data/link/` | `TcpGameLink` (et `InMemoryLinkPair` après 00-contrat) |
| `game/input/` | `InputMapper` + `InputConfig` (pur Kotlin) |
| `ui/lobby/` | écran de connexion : rôle, IP:port, état des capteurs, partenaire simulé |
| `docs/features/02-*`, `03-*`, `06-*` | tes specs et tâches |
Partagés (tu AJOUTES tes lignes, tu ne réécris pas celles de A) : `di/AppModule.kt`, `ui/navigation/`,
`res/values/strings.xml` (préfixe `lobby_` / `sensor_` / `link_`), `app/src/test/.../ui/catalog/ScreenCatalog.kt`,
`app/src/test/.../testing/`. Dans `AppModule.kt`, le bloc `// --- B ---` est à toi (lignes de `GameLinkFactory`,
`GameSession`, `SensorSource`, `InputSource`) : tu peux y REMPLACER les implémentations de départ par les tiennes.
**Pas à toi** : `game/engine/`, `ui/game/` (A) ; `game/contract/`, `AGENTS.md`, `tools/`, `.github/`, fichiers Gradle (commun).

## Tes features, dans l'ordre
0. `b/00-prise-en-main` (aucun code, une session par point) :
   0.1 installer (`docs/team/B-DEMARRAGE.md`) et obtenir `pwsh tools/check.ps1` = CHECK OK ;
   0.2 lire `docs/WORKFLOW.md` et `docs/decisions/` ;
   0.3 relire tes specs 02, 03, 06 et fixer les valeurs laissées libres (zone morte, seuil de lumière,
       « proche » pour la proximité, format IP:port) directement dans tes `spec.md` ;
   0.4 écrire tes questions pour A dans `docs/team/demandes.md`, commit, push, PR ;
   0.5 relire la PR `commun/00-contrat` de A (contrat suffisant pour 02, 03, 06 ?).
1. `b/02-liaison-tcp` — `docs/features/02-liaison-tcp/`
2. `b/03-capteurs` — `docs/features/03-capteurs/`
3. `b/06-lobby-connexion` — `docs/features/06-lobby-connexion/`
4. `07-integration` avec A (branche `commun/07-integration`).
Les features 02, 03 et 06 attendent que `commun/00-contrat` soit fusionnée dans `main` (le contrat doit exister).

## Après le contrat, ne jamais attendre A
Une fois `00-contrat` fusionnée, tu codes contre le CONTRAT, pas contre le code de A : `Engine` (le stub de 00 suffit), `InMemoryLinkPair`,
`FakeSensorSource`. Ce que tu produis pour A : des `Inputs` (via `InputMapper`) et des `NetMessage` (via `GameLink`).

## Skills (dossier `.agents/skills/`)
`android-hardware` (capteurs, TCP, InputMapper) · `android-screen` (lobby, ViewModel, Koin, navigation) · `gradle-build-fix`.

## Git (commandes exactes)
```
git switch main && git pull                      # toujours partir de main à jour
git switch -c b/02-liaison-tcp                   # nouvelle feature (ou: git switch b/02-liaison-tcp && git merge main)
pwsh tools/check.ps1                             # CHECK OK obligatoire avant chaque commit
git add <tes fichiers> && git commit -m "feat(02-liaison-tcp): TcpGameLink envoie et reçoit une ligne JSON"
git push                                         # setup-team a réglé push.autoSetupRemote : pas besoin de -u
                                                 # puis Pull Request vers main sur GitHub, relue par A
```
Si le hook répond `HORS PERIMETRE` ou `INTERDIT` : n'essaie pas de le contourner. Annule le fichier concerné,
écris le besoin dans `docs/team/demandes.md` et préviens l'humain.

## Fin de session (réponds avec ce format)
```
STATUT: OK | ECHEC | BLOQUE
BRANCHE: b/<NN-feature>   TACHE: <n> cochée dans tasks.md (prise en main : 0.<n>)
FICHIERS: <chemins>
VERIF: CHECK OK | <erreur exacte fichier:ligne>
DEMANDE_A_A: <rien | résumé écrit dans docs/team/demandes.md>
```
