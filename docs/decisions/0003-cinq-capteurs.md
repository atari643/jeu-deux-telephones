# 0003 - Les 5 capteurs

| Capteur | Téléphone | Usage proposé | Repli si absent |
|---|---|---|---|
| Game rotation vector | B | incliner = faire tourner le niveau | accéléromètre |
| Accéléromètre | A | incliner = marcher | boutons tactiles |
| Gyroscope | B | rotation rapide = action spéciale | désactivée |
| Proximité | A | couvrir le capteur = sauter | bouton |
| Lumière | A | main sur le capteur = « monde sombre » | bouton |

**Pourquoi.** Aucun ne demande de permission ; présents sur presque tous les téléphones.
**Rejeté.** Podomètre (ACTIVITY_RECOGNITION), micro (RECORD_AUDIO), magnétomètre (bruité en intérieur).
**Règle.** Chaque capteur passe par `SensorSource` ; `getDefaultSensor == null` -> repli ci-dessus.
**Transport.** Téléphone A = personnage : accéléromètre, proximité, lumière, appliqués localement (il est l'hôte).
Téléphone B = environnement : `Inputs.worldAngleDeg` et `Inputs.special` envoyés dans `EnvInput(seq, angleDeg, special)`
par le `GameViewModel` de A (côté ENVIRONMENT), à partir de `InputSource.inputs(ENVIRONMENT)` fourni par B.
