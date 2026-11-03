---
description: Installer et lancer l'app sur un appareil connecté (téléphone, appareil distant ou émulateur) et lire l'écran
agent: android-qa
subtask: true
---
Appareils :
!`pwsh tools/device.ps1 status`

Lancement :
!`pwsh tools/device.ps1 run`

Écran courant :
!`pwsh tools/device.ps1 ui`

Résume : l'app démarre-t-elle ? Que montre l'écran ? Crash éventuel (`pwsh tools/device.ps1 logs`) ?
Sans appareil, propose `/see <Ecran>` à la place.
