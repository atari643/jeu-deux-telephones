# 0001 - Liaison entre les téléphones : TCP sur le Wi-Fi local

**Décision.** Sockets `java.net` (ServerSocket sur le téléphone A, Socket sur B), une ligne JSON par
message (kotlinx-serialization), `tcpNoDelay = true`. A affiche son IP:port, B les saisit.
**Pourquoi.** Aucune nouvelle dépendance ni permission à l'exécution (INTERNET suffit en targetSdk 36),
latence de quelques ms en local, testable sur la JVM (InMemoryLinkPair + vraies sockets en 127.0.0.1).
**Rejeté.** Nearby Connections (dépendance Play Services + 4 permissions Bluetooth/Wi-Fi), Wi-Fi Direct
(appairage instable entre marques), Bluetooth (permissions + appairage).
**Conséquences.** Rester en targetSdk 36 (en 37 le réseau local demande une permission). Si le Wi-Fi de
l'école isole les appareils : B se connecte au partage de connexion de A. Découverte NSD possible plus tard.
