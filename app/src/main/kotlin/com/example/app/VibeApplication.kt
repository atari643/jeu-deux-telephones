package com.example.app

import android.app.Application
import com.example.app.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class VibeApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@VibeApplication)
            modules(appModule)
        }
    }
}
