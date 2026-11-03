# 0002 - Le téléphone A (personnage) fait autorité

**Décision.** Seul A exécute le moteur (`Engine.step`, 60 pas/s). B envoie `EnvInput(seq, angle)`
~30 fois/s ; A envoie `Snapshot(state)` ~20-30 fois/s. B dessine le dernier snapshot et applique sa
propre rotation tout de suite pour qu'elle paraisse instantanée.
**Pourquoi.** Les sauts doivent répondre sans délai ; une simulation en double (lockstep) casse au
moindre écart de calcul flottant. 50-100 ms de retard sur la rotation suffisent pour un jeu coopératif.
