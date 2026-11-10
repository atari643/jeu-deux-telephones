# Tâches 03-capteurs
Une tâche = 1 à 3 fichiers de prod + leurs tests, faisable dans une session neuve.
Cocher `[x]` seulement quand `pwsh tools/check.ps1` est vert. Chemins relatifs à `app/src/main/kotlin/com/example/app/`.

- [ ] 1. [B] Capteurs Android — `data/sensors/SensorTypeMapping.kt`, `data/sensors/AndroidSensorSource.kt` — test : `AndroidSensorSourceTest` Robolectric (CA1, CA2, CA3)
- [ ] 2. [B] Inclinaison -> moveX — `game/input/InputConfig.kt`, `game/input/InputMapper.kt` — test : `InputMapperTiltTest` (CA4)
- [ ] 3. [B] Saut, monde sombre, angle — `game/input/InputMapper.kt` — test : `InputMapperTest` (CA5)
- [ ] 4. [B] Source d'entrées — `game/input/SensorInputSource.kt` — test : `SensorInputSourceTest` (CA6)
- [ ] 5. [B] Câblage — `di/AppModule.kt` (SensorManager, `AndroidSensorSource`, `SensorInputSource`) — test : `AppModuleTest` vert
