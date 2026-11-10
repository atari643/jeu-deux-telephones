---
name: android-hardware
description: Code matériel du développeur B - capteurs Android (SensorManager + callbackFlow, repli si absent), liaison TCP entre les 2 téléphones (java.net, une ligne JSON par message), InputMapper capteurs -> Inputs, et leurs tests avec fakes/Robolectric. À charger pour toute tâche dans data/sensors/, data/link/ ou game/input/.
---

# Capteurs, liaison réseau, entrées (développeur B)

## Le contrat à respecter (game/contract/, FIGÉ)
`SensorSource.readings(): Flow<SensorReading>` · `SensorReading(type, values: List<Float>, timestampNs)`
· `SensorType { GAME_ROTATION, ACCELEROMETER, GYROSCOPE, PROXIMITY, LIGHT }`
· `GameLink { state: StateFlow<LinkState>; incoming: Flow<NetMessage>; send(m); close() }` · `GameLinkFactory { host(port); join(ip, port) }`
· `GameSession` (le lobby la remplit) · `InputSource.inputs(role)` (tu l'implémentes) · `NetCodec` (encode/décode une ligne JSON)
· `Inputs(moveX, jump, worldAngleDeg, darkWorld, special)`. Décisions : `docs/decisions/0001` (TCP), `0003` (capteurs).

## Capteurs — `data/sensors/AndroidSensorSource.kt`
```kotlin
class AndroidSensorSource(private val sensorManager: SensorManager) : SensorSource {
    override fun readings(): Flow<SensorReading> = callbackFlow {
        val listener = object : SensorEventListener {
            override fun onSensorChanged(e: SensorEvent) { trySend(e.toReading()) }
            override fun onAccuracyChanged(s: Sensor, accuracy: Int) = Unit
        }
        SensorType.entries.forEach { type ->
            sensorManager.getDefaultSensor(type.toAndroidType())?.let {    // null = capteur absent -> on saute
                sensorManager.registerListener(listener, it, SensorManager.SENSOR_DELAY_GAME)
            }
        }
        awaitClose { sensorManager.unregisterListener(listener) }
    }
}
```
- `fun SensorType.toAndroidType(): Int` vit dans `data/sensors/` (le contrat n'importe pas Android) :
  `Sensor.TYPE_GAME_ROTATION_VECTOR`, `TYPE_ACCELEROMETER`, `TYPE_GYROSCOPE`, `TYPE_PROXIMITY`, `TYPE_LIGHT`.
- Koin : `single { androidContext().getSystemService(SensorManager::class.java) }` + `singleOf(::AndroidSensorSource) { bind<SensorSource>() }`.
- Exposer aussi quels capteurs sont présents (`availableTypes()`) pour que le lobby affiche les replis.
- Test Robolectric : `shadowOf(sensorManager).addSensor(...)` puis `sendSensorEventToListeners(...)`
  (événement via `SensorEventBuilder`). Doute sur l'API -> `pwsh tools/docs.ps1 search "ShadowSensorManager"`.

## Entrées — `game/input/InputMapper.kt` (pur Kotlin, AUCUN import Android)
`fun map(readings: SensorSnapshot, previous: Inputs): Inputs` : inclinaison -> `moveX` (zone morte),
proximité proche -> `jump`, lumière basse -> `darkWorld`, rotation -> `worldAngleDeg`. Seuils dans `object InputConfig`.
Test JUnit pur : une lecture en entrée -> un `Inputs` attendu.

## Liaison — `data/link/TcpGameLink.kt`
- A (hôte) : `ServerSocket(port)` -> `accept()` ; B : `Socket(ip, port)`. Toujours sur `Dispatchers.IO`.
- `tcpNoDelay = true` ; lecture : `bufferedReader().lineSequence()` -> `NetCodec.decode` ; écriture : une ligne + `flush()`.
- Une coroutine lecture + une écriture ; tout fermer dans `finally` / `close()` ; erreurs -> état « déconnecté », jamais de crash.
- Tests : `InMemoryLinkPair` pour la logique ; un test sur vraies sockets en `127.0.0.1` port 0 (port libre) pour
  `TcpGameLink`. Flux testés avec Turbine (`incoming.test { assertEquals(msg, awaitItem()) }`).
- Pas de nouvelle permission : INTERNET suffit (targetSdk 36). IP locale de l'hôte : `WifiManager`/`ConnectivityManager`
  -> chercher la bonne API avec `tools/docs.ps1` avant d'écrire.

## Interdits
Nouvelle dépendance (Nearby, Ktor sockets...), `GlobalScope`, `Thread.sleep`, `runBlocking` hors tests, `!!`,
socket ou capteur dans un composable ou un ViewModel sans interface, modifier `game/contract/` ou `game/engine/`.
