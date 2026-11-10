# 02-liaison-tcp - Liaison entre les deux téléphones

Propriétaire : **B**  ·  Branche : `b/02-liaison-tcp`  ·  Décisions : 0001, 0002  ·  Skill : `android-hardware`
Prérequis : `00-contrat` fusionnée dans `main`.

## Objectif
Implémenter `GameLink` et `GameLinkFactory` avec des sockets TCP `java.net` (une ligne JSON par message via
`NetCodec`), robustes aux erreurs, plus la poignée de main `Hello` et l'IP locale à afficher par l'hôte.

## Critères d'acceptation
- CA1 : hôte (`host(port)`) et client (`join("127.0.0.1", port)`) : chaque message envoyé d'un côté est reçu de l'autre, dans l'ordre.
- CA2 : `join` vers une adresse sans serveur -> `state = DISCONNECTED` en moins de `LinkConfig.CONNECT_TIMEOUT_MS`, sans exception remontée.
- CA3 : si l'autre ferme, `incoming` se termine et `state = DISCONNECTED`.
- CA4 : poignée de main : le client envoie `Hello(role)`, l'hôte répond `Hello(son rôle)` ; deux rôles identiques -> fermeture + erreur `SAME_ROLE`.
- CA5 : `LocalAddressProvider.localIpv4()` renvoie l'IP Wi-Fi/partage de connexion (interface + impl Android ; fake en test).

## Hors périmètre
Découverte automatique (NSD), écran de connexion (06), mesures de latence avancées.
