---
name: code-quality
description: Checklist de revue de code Android Kotlin (architecture, Compose, coroutines, erreurs, tests, accessibilité, sécurité) et outils detekt/ktlint. À charger pour relire du code, faire une revue ou avant de déclarer une tâche terminée.
---

# Revue de qualité

## Outils automatiques (lancer d'abord)
```
pwsh tools/gradle.ps1 :app:spotlessApply :app:detekt
```
detekt ne détecte pas tout (pas de résolution de types) : la checklist ci-dessous reste obligatoire.

## Checklist (chaque point = OK / KO avec fichier:ligne)
### Architecture
- [ ] Respecte la structure `ui/<feature>/{Screen,UiState,ViewModel}`, `data/`, `domain/`, `di/`.
- [ ] `XScreen` sans état (pas de ViewModel/Koin/NavController dedans). ViewModel seulement dans `XRoute`.
- [ ] ViewModel : un seul `onEvent`, aucun `Context`. État exposé par `_state.asStateFlow()` ou `stateIn(...)`,
      jamais `val uiState: StateFlow<X> = _state` (le MutableStateFlow reste castable).
- [ ] Repository = interface + impl, retourne des modèles `domain`. Nouvelle classe déclarée dans AppModule.
- [ ] Aucune nouvelle dépendance, aucun fichier Gradle modifié.
### Kotlin
- [ ] Pas de `!!`, `lateinit` (prod), `GlobalScope`, `runBlocking`, `catch (e: Exception)`, `@Suppress`.
- [ ] `when` exhaustifs sans `else` sur sealed/enum. Pas de nombre magique (constantes nommées).
- [ ] Fonctions courtes (≤ 40 lignes), noms explicites, pas de code mort ni commenté.
### Compose / UI
- [ ] Tout élément interactif a un `testTag` venant de `XTags`. Tags uniques dans les listes (`item(id)`).
- [ ] Textes dans `strings.xml`. `contentDescription` sur les icônes non décoratives.
- [ ] `LazyColumn` avec `key = { it.id }`. États chargement / vide / erreur gérés.
- [ ] `@Preview` + entrée dans `ScreenCatalog.kt` pour chaque état important.
### Tests
- [ ] Chaque Event du ViewModel a au moins un test. Chaque écran a un test d'affichage + un test d'événement.
- [ ] Les tests utilisent des fakes, `MainDispatcherRule`, ViewModel créé dans `@Before`.
### Sécurité / données
- [ ] Pas de secret/clé API en dur. Pas de `Log.d` avec données personnelles. Requêtes en `https`.

## Format du rapport
```
REVUE: OK | A_CORRIGER
BLOQUANT:
- app/src/.../X.kt:42 [règle] problème -> correction proposée
AMELIORATION (non bloquant):
- ...
```
Ne liste que ce qui est concret et vérifié dans le code. Pas de remarques génériques.
