# Tâches 02-liaison-tcp
Une tâche = 1 à 3 fichiers de prod + leurs tests, faisable dans une session neuve.
Cocher `[x]` seulement quand `pwsh tools/check.ps1` est vert. Chemins relatifs à `app/src/main/kotlin/com/example/app/`.

- [ ] 1. [B] Liaison TCP de base — `data/link/TcpGameLink.kt`, `data/link/TcpGameLinkFactory.kt`, `data/link/LinkConfig.kt` — test : `TcpGameLinkTest` sur 127.0.0.1 port 0 (CA1)
- [ ] 2. [B] Erreurs et fermeture — `TcpGameLink.kt`, `TcpGameLinkFactory.kt` — test : `TcpGameLinkErrorTest` (CA2, CA3)
- [ ] 3. [B] Poignée de main — `data/link/Handshake.kt` — test : `HandshakeTest` avec `InMemoryLinkPair` (CA4)
- [ ] 4. [B] IP locale — `data/link/LocalAddressProvider.kt` (interface + impl Android) — test : `LocalAddressProviderTest` Robolectric ou fake (CA5)
- [ ] 5. [B] Câblage — `di/AppModule.kt`, bloc `// --- B ---` (remplacer la fabrique mémoire par `TcpGameLinkFactory`) — test : `AppModuleTest` vert
