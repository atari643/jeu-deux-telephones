---
name: compose-ui
description: Patterns Jetpack Compose Material3 du projet (Route/Screen sans état, state hoisting, testTag, Preview, listes, formulaires, mises en page, accessibilité). À charger pour écrire ou modifier un composable ou un écran.
---

# Compose UI

## Structure imposée d'un fichier écran (copie `ui/notes/NotesScreen.kt`)
```kotlin
object ProfileTags {                      // 1. tags (constantes)
    const val NAME = "profile_name"
    const val SAVE = "profile_save"
    fun item(id: Long) = "profile_item_$id"
}

@Composable                               // 2. Route : SEUL endroit avec le ViewModel
fun ProfileRoute(viewModel: ProfileViewModel = koinViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    ProfileScreen(state = state, onEvent = viewModel::onEvent)
}

@Composable                               // 3. Screen SANS état : state in, events out
fun ProfileScreen(state: ProfileUiState, onEvent: (ProfileEvent) -> Unit, modifier: Modifier = Modifier) { ... }

object ProfilePreviewData { val default = ProfileUiState(isLoading = false, name = "Ada") }  // 4.

@Preview(showBackground = true)           // 5.
@Composable
private fun ProfileScreenPreview() { AppTheme { ProfileScreen(ProfilePreviewData.default, onEvent = {}) } }
```

## Règles
- `Screen` ne connaît ni ViewModel, ni NavController, ni Koin, ni repository.
- Chaque élément cliquable / saisissable / important a `Modifier.testTag(XTags.ELEMENT)`.
  Le modifier passé en paramètre s'applique au composable RACINE uniquement.
- `remember { mutableStateOf() }` seulement pour de l'état purement visuel (menu ouvert, onglet).
  Toute donnée métier vient de `state`.
- Textes : `stringResource(R.string.x)` ; avec argument : `stringResource(R.string.x, valeur)`.
- Icônes : `Icons.Filled.X` de `androidx.compose.material.icons.filled.*` — seules les icônes
  "core" existent : Add, Delete, Edit, Check, Close, Clear, Done, Menu, Search, Settings, Favorite,
  Home, Person, Star, Info, Warning, MoreVert, Refresh, Share, Email, Phone, Place, Lock,
  KeyboardArrowDown/Up. Retour : `Icons.AutoMirrored.Filled.ArrowBack`
  (import `androidx.compose.material.icons.automirrored.filled.ArrowBack`). Aucune autre icône.
  Toujours `contentDescription` (texte de strings.xml) ou `null` si décorative.
- Espacements : `Arrangement.spacedBy(8.dp)` / `padding(16.dp)`. Pas de tailles en px.
- Experimental Material3 (TopAppBar, etc.) : `@OptIn(ExperimentalMaterial3Api::class)` sur la fonction.

## Briques usuelles
```kotlin
Scaffold(topBar = { TopAppBar(title = { Text(stringResource(R.string.t)) }) }) { padding ->
    Column(Modifier.fillMaxSize().padding(padding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) { }
}
LazyColumn(Modifier.testTag(XTags.LIST)) {
    items(state.items, key = { it.id }) { item -> ListItem(headlineContent = { Text(item.name) }, modifier = Modifier.testTag(XTags.item(item.id))) }
}
OutlinedTextField(value = state.name, onValueChange = { onEvent(XEvent.NameChanged(it)) },
    label = { Text(stringResource(R.string.name)) }, singleLine = true, modifier = Modifier.testTag(XTags.NAME))
Button(onClick = { onEvent(XEvent.SaveClicked) }, enabled = !state.isSaving, modifier = Modifier.testTag(XTags.SAVE)) { Text(...) }
Switch(checked = state.dark, onCheckedChange = { onEvent(XEvent.DarkChanged(it)) }, modifier = Modifier.testTag(XTags.DARK))
if (state.isLoading) CircularProgressIndicator(Modifier.testTag(XTags.LOADING))
```
États à gérer dans `when` : chargement, vide, erreur, contenu (voir `NotesScreen`).

## Accessibilité / mise en page
- Cibles tactiles ≥ 48dp (Button/IconButton le garantissent ; pas de `Modifier.size(24.dp).clickable`).
- Largeur d'écran de référence : 411dp. Rien ne doit dépasser x + largeur > 411.
- Texte long : `maxLines = 1, overflow = TextOverflow.Ellipsis`.

## Vérifier
`pwsh tools/see.ps1 <XScreen>` (après ajout dans `ScreenCatalog.kt`) — skill `ui-verification`.
