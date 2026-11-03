---
name: gradle-build-fix
description: Procédure pour corriger une erreur de compilation Kotlin, KSP/Room, ressource, Koin ou un test rouge, avec la table des erreurs fréquentes et leur correction. À charger dès que tools/gradle.ps1 affiche BUILD ECHEC.
---

# Corriger un BUILD ECHEC

## Procédure (3 essais maximum)
1. Lis la sortie de `tools/gradle.ps1` : chaque erreur = `fichier:ligne:colonne message`.
2. Traite UNIQUEMENT la PREMIÈRE erreur (les suivantes en découlent souvent).
3. Ouvre le fichier autour de la ligne (±15 lignes). Corrige au plus petit endroit possible.
4. Recompile : `pwsh tools/gradle.ps1 :app:compileDebugKotlin`.
5. Même erreur après 3 essais -> STOP. Rapporte : `BLOQUE: <fichier:ligne> <message> | essais: ...`.
Besoin de plus de détails : grep dans `build/logs/last.log` (ne jamais l'afficher en entier).

## Interdit pour "faire passer"
Modifier `*.gradle.kts` / versions, ajouter une dépendance, `@Suppress`, supprimer/ignorer un test,
`!!`, cast forcé `as X`, commenter du code, ré-enregistrer les snapshots.

## Erreurs fréquentes
| Message (extrait) | Cause -> correction |
|---|---|
| `Unresolved reference 'X'` | Import manquant ou nom faux. Cherche `class X`/`fun X` dans `app/src`, sinon `tools/docs.ps1 search "X"`. Compose : voir imports du skill `kotlin-android-rules`. |
| `Unresolved reference 'R'` / `'string'` | `import com.example.app.R` ; la ressource doit exister dans `res/values/strings.xml`. |
| `@Composable invocations can only happen from the context of a @Composable function` | Appel composable dans un lambda non composable (`onClick`). Sortir l'appel ou n'y mettre que `onEvent(...)`. |
| `Argument type mismatch` / `Type mismatch: inferred type is A but B was expected` | Lis le type attendu, convertis (`.toString()`, `.toLong()`, `?: valeurParDefaut`). |
| `No parameter with name 'x' found` / `No value passed for parameter 'y'` | Ouvre la déclaration de la fonction appelée et aligne les noms. |
| `None of the following candidates is applicable` | Mauvaise combinaison d'arguments : regarde la signature (docs ou code existant). |
| `'when' expression must be exhaustive` | Nouvel Event/enum ajouté : ajoute sa branche (pas de `else`). |
| `Suspend function 'x' should be called only from a coroutine` | Dans le ViewModel : `viewModelScope.launch { x() }`. |
| `Smart cast to 'X' is impossible` | Copier dans une `val` locale avant le test `if (v != null)`. |
| `Only safe (?.) or non-null asserted (!!.) calls are allowed` | Utilise `?.` / `?:` (jamais `!!`). |
| `This material API is experimental` / `OptIn` | `@OptIn(ExperimentalMaterial3Api::class)` sur la fonction. |
| `Cannot access 'X': it is private` | Utilise l'API publique, ou rends `internal` si c'est ton propre code. |
| `Conflicting declarations` / `Redeclaration` | Même nom deux fois dans le package : renomme ou supprime le doublon. |
| `[ksp] ... Cannot figure out how to save this field` | Type non supporté par Room : utiliser String/Long/Int/Boolean. |
| `[ksp] There is a problem with the query` | Nom de table/colonne faux dans `@Query` (comparer avec `@Entity`). |
| `MissingKoinDefinitionException ... type:'X'` | Ajouter la définition de X dans `di/AppModule.kt` (skill `mvvm-koin`). |
| `Module with the Main dispatcher is missing` | Test de ViewModel sans `MainDispatcherRule`, ou ViewModel créé hors `@Before`. |
| `Expected exactly '1' node but found '2'` | testTag non unique : utiliser `XTags.item(id)`. |
| `Failed: assertExists ... could not find any node` | Mauvais tag/texte, ou élément hors écran (`performScrollToNode`), ou `useUnmergedTree = true`. |
| `AAPT: error: resource string/x not found` | Ajouter `<string name="x">` dans `strings.xml`. |
| `Roborazzi: The original file(...X.png) was not found` | Nouvel écran du catalogue sans image de référence : `pwsh tools/gradle.ps1 :app:recordRoborazziDebug`. |
| `Roborazzi: ...X.png is changed` | Écran modifié volontairement par la tâche -> `recordRoborazziDebug` ; sinon régression à corriger. |
| `FORMATAGE : violations ktlint` | `pwsh tools/gradle.ps1 :app:spotlessApply` (automatique). |
| Avertissement `w: ... is deprecated` | Non bloquant : ne pas corriger sauf demande. |
