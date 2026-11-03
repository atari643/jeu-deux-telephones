---
name: ui-verification
description: Voir et vérifier visuellement le travail sans images - lire l'arbre UI texte de tools/see.ps1, détecter les problèmes de mise en page, comparer les snapshots Roborazzi, utiliser un appareil réel (device.ps1 / MCP mobile). À charger pour vérifier un écran ou des critères d'acceptation.
---

# Vérifier l'UI (le modèle ne voit pas les images : tout passe par du TEXTE)

## 1. Arbre UI sans émulateur (méthode principale)
```
pwsh tools/see.ps1 NotesScreen          # tous les états NotesScreen_* du catalogue
pwsh tools/see.ps1 NotesScreen -NoBuild # relire sans relancer Gradle
```
L'écran doit être déclaré dans `app/src/test/kotlin/com/example/app/ui/catalog/ScreenCatalog.kt`.

Lecture d'une ligne :
```
[notes_add] Button {click} @(300,92 95x40dp)
 tag         rôle   actions  x,y   largeur x hauteur (dp, écran 411x891dp)
"Texte"  champ="valeur saisie"  desc="contentDescription"  DESACTIVE  Selected=true  ToggleableState=On
```
L'indentation = imbrication. `!! Elements interactifs SANS testTag` = défaut à corriger.

## 2. Contrôles systématiques sur l'arbre
| Contrôle | Comment | Problème si |
|---|---|---|
| Critère d'acceptation | chercher le tag / texte attendu | absent ou mauvais texte |
| Débordement | x + largeur | > 411 |
| Chevauchement | deux frères avec des rectangles qui se croisent | se croisent |
| Cible tactile | élément `{click}` | largeur ou hauteur < 48 (sauf texte cliquable) |
| Ordre visuel | y croissants dans l'ordre attendu | ordre incohérent |
| État vide / erreur / chargement | entrées `_empty`, `_error`, `_loading` du catalogue | état non géré |
| Texte brut | texte affiché = valeur de strings.xml | clé ou texte en dur visible |

## 3. Régression visuelle (images comparées par l'outil, résultat en texte)
- `pwsh tools/gradle.ps1 :app:verifyRoborazziDebug` -> `ROBORAZZI : n écrans, k différents`.
- Différence VOULUE (on a changé cet écran) : `pwsh tools/gradle.ps1 :app:recordRoborazziDebug`.
- Différence sur un écran qu'on n'a PAS touché = régression : la signaler, ne pas ré-enregistrer.

## 4. Appareil réel (seulement si `pwsh tools/device.ps1 status` montre un appareil)
```
pwsh tools/device.ps1 run    # installe + lance, détecte un crash au démarrage
pwsh tools/device.ps1 ui     # arbre de l'écran courant
pwsh tools/device.ps1 logs   # crash + erreurs de l'app
```
Si le MCP `mobile` est activé (agent android-qa) : `mobile_list_elements_on_screen` pour lire
l'écran, `mobile_click_on_screen_at_coordinates` / `mobile_type_keys` pour interagir, puis relire.
Ne jamais demander de capture d'écran (image illisible pour le modèle).
Sur cette machine (VM) l'émulateur ne démarre pas : sans appareil, rester sur la méthode 1.

## Rapport
```
QA: OK | KO
CA1 ✅ <preuve : ligne de l'arbre>
CA2 ❌ <attendu> / <observé> -> <fichier probable>
MISE EN PAGE: <problèmes ou "rien">
```
