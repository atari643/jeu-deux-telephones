package com.example.app.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

/**
 * Client HTTP unique de l'app. Un service distant = une classe `XxxApi(private val client: HttpClient)`
 * avec des fonctions `suspend` qui renvoient des DTO @Serializable. En test : `MockEngine`.
 */
fun createHttpClient(engine: HttpClientEngine = OkHttp.create()): HttpClient = HttpClient(engine) {
    // Reponse non 2xx => ResponseException (a attraper dans le repository)
    expectSuccess = true
    install(ContentNegotiation) {
        json(Json { ignoreUnknownKeys = true })
    }
}
