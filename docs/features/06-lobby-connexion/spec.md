# 06-lobby-connexion - Écran de connexion entre les deux téléphones

Propriétaire : **B**  ·  Branche : `b/06-lobby-connexion`  ·  Skills : `android-screen`, `android-hardware`
Prérequis : `02-liaison-tcp` et `03-capteurs` fusionnées.

## Objectif
Le lobby permet de choisir son rôle, d'héberger (CHARACTER) ou de rejoindre (ENVIRONMENT), voit les capteurs
disponibles, puis remplit `GameSession` et ouvre l'écran de jeu de A.

## Critères d'acceptation
- CA1 : choix du rôle Personnage / Environnement (boutons avec testTags).
- CA2 : CHARACTER -> « Héberger » affiche `IP:port` (via `LocalAddressProvider`) et « En attente » ; connexion reçue -> `GameSession.start` + navigation vers Game.
- CA3 : ENVIRONMENT -> champ `IP:port` validé (IPv4 en 4 nombres 0-255, port 1024-65535) ; « Rejoindre » ; échec -> message d'erreur ; succès -> Game.
- CA4 : liste des 5 capteurs : présent / absent + repli prévu (décision 0003).
- CA5 : bouton « Partenaire simulé » présent (son fonctionnement complet = 07).
- CA6 : snapshots `LobbyScreen_default`, `LobbyScreen_waiting`, `LobbyScreen_error` ; `tools/see.ps1 LobbyScreen` sans problème.

## Hors périmètre
Découverte automatique de l'hôte, comptes, plusieurs parties.
