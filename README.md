# Jeu de plateforme coopératif à 2 téléphones (Android)

Téléphone A : le personnage (déplacements, sauts). Téléphone B : l'environnement (faire tourner le niveau).
Développé uniquement avec des IA. Règles pour les IA : `AGENTS.md`. Workflow expliqué : `docs/WORKFLOW.md`.

## 1. Outils à installer
| Outil | Qui | Pourquoi |
|---|---|---|
| Git + GitHub CLI (`gh auth login`) | A et B | Branches, PR, CI |
| JDK 21 + SDK Android | A et B | `pwsh tools/setup-android.ps1` (Windows) ou Android Studio |
| Android Studio | A et B | Ouvrir le projet, Logcat, aperçu Compose, miroir du téléphone |
| PowerShell 7 (`pwsh`) | A et B | Tous les scripts `tools/` (existe aussi sur Mac/Linux) |
| opencode + llama-server + Qwen3.8-27B | A | IA locale (voir §3) |
| Une IA au choix | B | Gemini dans Android Studio (gratuit, lit AGENTS.md), Codex, Cursor, Copilot... |
| Tailscale (PC + téléphone) | A | Déboguer le téléphone depuis le Shadow PC (§4) |
| scrcpy (facultatif) | A et B | Afficher le téléphone sur le PC |

Après le clone, une fois : `pwsh tools/setup-team.ps1 -Role A` (ou `-Role B`) — rôle, protections git, vérification
complète (doit finir par `CHECK OK`). Diagnostic : `pwsh tools/doctor.ps1`.

## 2. Travailler (les deux)
```powershell
git switch -c a/01-moteur                    # a/ pour A, b/ pour B (préfixe obligatoire)
# demander à son IA : "fais la tâche 3 de docs/features/01-moteur/tasks.md"
pwsh tools/check.ps1                          # CHECK OK avant chaque commit
git push; gh pr create                       # l'autre relit, le CI doit être vert
```

## 3. A : opencode + Qwen local (dans Android Studio)
1. Lancer le modèle (terminal séparé) : `& "$env:USERPROFILE\.config\opencode\start-qwen.ps1"` (contexte 64k).
2. Ouvrir ce dossier dans Android Studio, puis dans son **terminal intégré** : `opencode`.
   Studio recharge tout seul les fichiers que l'IA modifie ; vous voyez le résultat dans Preview/Logcat.
3. Commandes opencode : `/task 00-contrat 1`, `/check`, `/fix`, `/see GameScreen`, `/review`, `/pr`, `/docs <mots>`.
   Une tâche = une session neuve (`/new`).
4. Facultatif : l'IA de Studio peut utiliser le même serveur (Settings > Tools > AI > Model Providers >
   Local Provider, port 1234). Déconseillé en même temps qu'opencode : un seul slot GPU.
5. Mémoire du Shadow PC : llama-server + Studio + Gradle saturent la mémoire. Fermer Studio pendant
   `tools/check.ps1` si un test plante avec « fichier de pagination ».

## 4. Les deux téléphones
- **Installer le jeu sans câble** : sur GitHub > Actions > dernier run vert > artifact `jeu-debug-apk`,
  télécharger sur le téléphone, dézipper, installer (autoriser les sources inconnues). Tous les APK
  (CI, PC de A, PC de B) ont la même clé de debug (`app/debug.keystore`) : ils s'installent par-dessus.
- **Jouer à deux** : les 2 téléphones sur le même Wi-Fi. Si le Wi-Fi de l'école bloque, A active son
  partage de connexion et B s'y connecte. A (hôte) affiche IP:port, B les saisit.
- **Tester seul** : mode « Partenaire simulé » dans le lobby (l'autre rôle est joué par un bot).
- **Déboguer depuis le Shadow PC** (pas d'émulateur possible : pas de virtualisation imbriquée) :
  1. Tailscale sur le PC et sur le téléphone (même compte).
  2. Téléphone : Options développeur > Débogage sans fil > Associer avec un code.
  3. PC : `adb pair <ip-tailscale>:<port-association>` puis `adb connect <ip-tailscale>:<port>`.
  4. `pwsh tools/device.ps1 status` doit voir le téléphone, puis `run`, `ui`, `logs`.
  Si ça ne passe pas : redirection USB du client Shadow, ou APK via le CI + Logcat chez B.
- **B avec un PC normal** : peut aussi lancer 2 émulateurs dans Android Studio (Device Manager) pour
  tester la liaison sans téléphones.

## Structure
`AGENTS.md` (règles IA) · `docs/WORKFLOW.md` · `docs/decisions/` (ADR) · `docs/features/` (specs + tâches)
· `docs/team/` (fiches A et B, demandes) · `tools/` (scripts filtrés pour les IA) · `.github/` (CI, PR, `ownership.txt`)
· `.githooks/` (protections locales) · `.agents/skills/` (skills des deux IA) · `.opencode/` (config de A).

**Nouveau développeur B ?** Commence par `docs/team/B-DEMARRAGE.md`.
