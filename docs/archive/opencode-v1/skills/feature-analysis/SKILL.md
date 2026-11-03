---
name: feature-analysis
description: Analyser une demande de fonctionnalité mobile et produire une spec courte dans docs/specs/ (écrans, UiState, événements, données, critères d'acceptation testables, découpage en tâches atomiques). À charger avant de développer une nouvelle feature ou quand la demande est floue.
---

# Analyse -> spec `docs/specs/<feature>.md`

## Démarche
1. Reformule la demande en 1 phrase. Si une information indispensable manque, liste-la dans
   `QUESTIONS` (max 3) et propose une hypothèse par défaut raisonnable : ne bloque pas.
2. Lis l'existant utile : `AppNavHost.kt`, `AppModule.kt`, la feature la plus proche (`ui/notes`).
3. Reste MINIMAL : la plus petite version qui répond à la demande. Pas de fonctionnalité bonus.
4. Uniquement avec la stack du projet (Compose, MVVM, Koin, Room, Ktor). Rien d'autre.

## Template (copier exactement, remplir)
```markdown
# Spec : <Feature>
Objectif : <1 phrase>
Hypothèses : <liste courte>
QUESTIONS : <aucune | liste>

## Écrans
- <Feature>Screen : <ce qu'on voit>. Navigation : depuis <X> via <action>.

## État (<Feature>UiState)
| champ | type | défaut | rôle |
## Événements (<Feature>Event)
| événement | effet |
## Erreurs (<Feature>Error)
| valeur | quand | texte affiché |
## Données
- Modèle : <data class ...>   - Source : Room <table> | Ktor <url> | aucune
- Repository : <interface + méthodes>

## testTags
| tag | élément |

## Critères d'acceptation (chacun vérifiable par test ou par tools/see.ps1)
- [ ] CA1 : Étant donné <état>, quand <action>, alors <résultat observable>.
- [ ] CA2 : ...

## Tâches (ordre d'exécution, 1 à 3 fichiers chacune)
1. [dev] domain/model/... + data/... (+ fake de test)
2. [dev] ui/<feature>/<Feature>UiState.kt
3. [dev] ui/<feature>/<Feature>ViewModel.kt + ligne Koin
4. [test] <Feature>ViewModelTest
5. [dev] ui/<feature>/<Feature>Screen.kt + strings.xml
6. [dev] navigation + ScreenCatalog.kt
7. [test] <Feature>ScreenTest + recordRoborazziDebug
8. [qa] vérification des CA avec tools/see.ps1
```
Critères d'acceptation : observables (texte, tag, état), jamais "le code est propre" ou "c'est joli".
