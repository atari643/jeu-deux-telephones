# 03-capteurs - Les 5 capteurs et leur traduction en commandes

Propriétaire : **B**  ·  Branche : `b/03-capteurs`  ·  Décisions : 0003  ·  Skill : `android-hardware`
Prérequis : `00-contrat` fusionnée dans `main`.

## Objectif
Lire les 5 capteurs (`AndroidSensorSource`), les traduire en `Inputs` selon le rôle (`InputMapper`, pur Kotlin),
et fournir `InputSource` au jeu de A. Repli propre si un capteur manque.

## Répartition
Téléphone CHARACTER : accéléromètre (marcher), proximité (sauter), lumière (monde sombre).
Téléphone ENVIRONMENT : game rotation vector (angle du monde), gyroscope (rotation rapide = `special`).

## Critères d'acceptation
- CA1 : `AndroidSensorSource` enregistre les capteurs présents en `SENSOR_DELAY_GAME` et émet des `SensorReading` (Robolectric).
- CA2 : capteur absent -> ignoré, et absent de `availableTypes()`.
- CA3 : quand le collecteur s'arrête, les listeners sont désenregistrés.
- CA4 : `InputMapper` : inclinaison au-delà de la zone morte -> `moveX` dans [-1, 1] ; dans la zone morte -> 0.
- CA5 : proximité « proche » -> `jump` (sur le front montant) ; lumière < seuil -> `darkWorld` ; rotation -> `worldAngleDeg` ;
  sans game rotation vector -> angle calculé depuis l'accéléromètre.
- CA6 : `SensorInputSource.inputs(role)` combine `SensorSource` + `InputMapper` -> `Flow<Inputs>` (test : `FakeSensorSource` + Turbine).

## Hors périmètre
Calibration par l'utilisateur, micro, podomètre (rejetés dans 0003).
