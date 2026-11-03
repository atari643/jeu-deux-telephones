---
name: mvvm-koin
description: Architecture MVVM du projet - ViewModel avec StateFlow et onEvent, repositories Room/Ktor, injection Koin dans AppModule, appels réseau Ktor. À charger pour écrire un ViewModel, un repository, une DAO Room, un appel réseau ou modifier AppModule.
---

# MVVM + Koin + Room + Ktor

## ViewModel (modèle : `ui/notes/NotesViewModel.kt`)
```kotlin
class ProfileViewModel(private val repository: ProfileRepository) : ViewModel() {
    private val _state = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _state.asStateFlow()

    init { load() }

    fun onEvent(event: ProfileEvent) {
        when (event) {
            is ProfileEvent.NameChanged -> _state.update { it.copy(name = event.name, error = null) }
            ProfileEvent.SaveClicked -> save()
        }
    }

    private fun load() {
        viewModelScope.launch {
            val profile = repository.getProfile()
            _state.update { it.copy(isLoading = false, name = profile.name) }
        }
    }

    private fun save() {
        val name = _state.value.name.trim()
        if (name.isEmpty()) { _state.update { it.copy(error = ProfileError.BlankName) }; return }
        viewModelScope.launch { repository.saveName(name) }
    }
}
```
Données qui changent dans le temps (liste Room) -> préférer le pattern `combine(...).stateIn(...)` de `NotesViewModel`.

## Repository
Interface (ce que le ViewModel voit) + implémentation `RoomXxxRepository` / `RemoteXxxRepository`.
Retourne des modèles `domain/model`, jamais des `Entity` ni des DTO.

## Room
```kotlin
@Entity(tableName = "profiles") data class ProfileEntity(@PrimaryKey(autoGenerate = true) val id: Long = 0, val name: String)
@Dao interface ProfileDao {
    @Query("SELECT * FROM profiles") fun observeAll(): Flow<List<ProfileEntity>>
    @Query("SELECT * FROM profiles WHERE id = :id") suspend fun getById(id: Long): ProfileEntity?
    @Insert suspend fun insert(e: ProfileEntity): Long
    @Update suspend fun update(e: ProfileEntity)
    @Query("DELETE FROM profiles WHERE id = :id") suspend fun deleteById(id: Long)
}
```
Puis : ajouter l'entité à `@Database(entities = [...])`, `version + 1`, exposer `abstract fun profileDao()`,
et dans AppModule : `single { get<AppDatabase>().profileDao() }`.
Types de colonnes : String, Int, Long, Boolean, Double, nullable. Pas de List/objet (sinon TypeConverter).

## Réseau Ktor (client déjà fourni par Koin : `HttpClient`)
```kotlin
@Serializable data class UserDto(val id: Long, val name: String)

class UserApi(private val client: HttpClient) {
    suspend fun getUser(id: Long): UserDto = client.get("https://api.example.com/users/$id").body()
}
```
Imports : `io.ktor.client.HttpClient`, `io.ktor.client.call.body`, `io.ktor.client.request.get`,
`kotlinx.serialization.Serializable`. Erreurs : `catch (e: IOException)` et `catch (e: ResponseException)`.
Test : `createHttpClient(MockEngine { respond(content = "{...}", headers = headersOf(HttpHeaders.ContentType, "application/json")) })`.

## Koin — `di/AppModule.kt`
```kotlin
single { get<AppDatabase>().profileDao() }                 // objet fourni par une autre instance
singleOf(::UserApi)                                         // classe concrète
singleOf(::RoomProfileRepository) { bind<ProfileRepository>() }   // impl -> interface
viewModelOf(::ProfileViewModel)                             // ViewModel
```
Le test `AppModuleTest` vérifie le module : s'il échoue avec `MissingKoinDefinitionException`,
il manque une ligne ci-dessus pour le type cité.
