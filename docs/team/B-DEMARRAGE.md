# Démarrage du développeur B (guide pour l'humain)

Bienvenue ! Toi et ton IA (Gemini dans Android Studio), vous codez **le matériel du jeu** : capteurs, liaison
entre les 2 téléphones, traduction capteurs -> commandes du jeu, écran de lobby. A code le moteur et le rendu.
Tout est organisé pour que vous ne vous marchiez jamais dessus : chacun ses dossiers, ses branches, et GitHub
refuse le reste. Lis ce guide une fois (10 minutes), ensuite tu n'utilises que le §4.

## 1. Installation (une fois)
1. **Android Studio** (dernière version stable) : https://developer.android.com/studio — il installe aussi le JDK et le SDK.
2. **Git** : https://git-scm.com (Windows : garder les options par défaut, dont « Git Bash »).
3. **PowerShell 7** (`pwsh`) : Windows `winget install Microsoft.PowerShell` ; Mac `brew install powershell`.
4. **Compte GitHub** : envoie ton pseudo à A ; accepte l'invitation reçue par mail (ou https://github.com/notifications).
5. **Un téléphone Android** : Options développeur activées (Paramètres > À propos > appuyer 7 fois sur « Numéro de build »).

## 2. Récupérer le projet (une fois)
```
git clone https://github.com/atari643/jeu-deux-telephones.git
cd jeu-deux-telephones
pwsh tools/setup-team.ps1 -Role B
```
`setup-team` enregistre ton rôle (B), installe les **protections git** (impossible de committer sur `main` ou sur
les fichiers de A) et lance la vérification complète. Elle doit finir par `CHECK OK` (premier lancement : quelques minutes).
Puis ouvre le dossier dans Android Studio (File > Open).

## 3. Préparer Gemini (une fois)
1. Android Studio > fenêtre **Gemini** > se connecter avec un compte Google > activer **Agent** (Agent Mode).
2. Dans le chat, panneau **Context** : vérifier que `AGENTS.md` est coché (il est lu automatiquement à chaque message).
3. Les skills du projet sont dans `.agents/skills/` : Gemini les trouve tout seul (`android-hardware` pour toi).
4. Permissions (Settings > Tools > AI > Agent Permissions) : autoriser l'écriture des fichiers du projet et les
   commandes `pwsh tools/...` et `git ...`. **Refuser** toute commande contenant `--no-verify` ou `--force`.

## 4. Chaque session de travail (le seul paragraphe à retenir)
1. Ouvre un **nouveau chat** Gemini (un chat = une tâche) et colle ce message en changeant la feature et le numéro :
   > Je suis le développeur B. Lis AGENTS.md puis docs/team/B.md. Mets-toi sur la branche b/02-liaison-tcp
   > (crée-la depuis main à jour si elle n'existe pas, sinon fusionne main dedans). Fais UNIQUEMENT la tâche 1 de
   > docs/features/02-liaison-tcp/tasks.md : test d'abord, puis le code, puis pwsh tools/check.ps1.
   > Coche la tâche, fais le commit, et réponds avec le format « Fin de session » de B.md.
2. Relis ce qu'il a fait (onglet des changements), puis tâche suivante dans un nouveau chat.
3. Feature terminée : `git push`, puis sur GitHub **Compare & pull request** vers `main`. Remplis le modèle.
   A relit et approuve ; le CI (jobs `check` et `scope`) doit être vert ; puis **Squash and merge**.

## 5. Ta toute première branche : `b/00-prise-en-main` (déjà créée, aucun code)
```
git switch main && git pull
git switch b/00-prise-en-main && git merge main
```
Puis, une session par point 0.1 à 0.5 de `docs/team/B.md`, par exemple : *« Je suis B. Lis AGENTS.md et
docs/team/B.md. Fais le point 0.3 de la prise en main sur la branche b/00-prise-en-main. »* Valide, commit, push, PR. Pendant ce temps A prépare
`commun/00-contrat` : relis sa PR avec Gemini (« relis cette PR contre AGENTS.md et docs/features/00-contrat/spec.md »).

## 6. Problèmes fréquents
| Message | Que faire |
|---|---|
| `HORS PERIMETRE ... appartient à A` | Gemini a touché un fichier de A : `git restore --staged <f> && git restore <f>`, écrire le besoin dans `docs/team/demandes.md`. |
| `INTERDIT : pas de commit sur main` | `git switch -c b/<NN-feature>` puis recommencer le commit. |
| `ROLE INCONNU` | `pwsh tools/setup-team.ps1 -Role B -SkipCheck` |
| CI rouge sur la PR | Ouvrir le job en échec, copier les lignes d'erreur à Gemini ; en local `pwsh tools/check.ps1`. |
| Conflit en fusionnant `main` | Garder les deux blocs de lignes (fichiers partagés = chacun ajoute ses lignes), puis `pwsh tools/check.ps1`. |
| Besoin de changer `game/contract/` | Écrire la demande dans `docs/team/demandes.md` ; A ou B ouvre une branche `commun/<sujet>`, PR approuvée par les deux. |

## 7. Tester sur les téléphones
- APK sans câble : GitHub > Actions > dernier run vert > artifact `jeu-debug-apk`.
- Depuis Android Studio : téléphone branché en USB (ou débogage sans fil) > bouton Run.
- Jouer à deux : même Wi-Fi (ou partage de connexion du téléphone de A).
