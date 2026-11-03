package com.example.app.di

import android.content.Context
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.HttpClientEngine
import org.junit.Test
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.test.verify.verify

/** Echoue si une classe injectee par constructeur n'a pas sa dependance declaree dans appModule. */
class AppModuleTest {
    @OptIn(KoinExperimentalAPI::class)
    @Test
    fun `app module is complete`() {
        // extraTypes = types fournis par Android/les librairies, pas par appModule
        appModule.verify(extraTypes = listOf(Context::class, HttpClientEngine::class, HttpClientConfig::class))
    }
}
