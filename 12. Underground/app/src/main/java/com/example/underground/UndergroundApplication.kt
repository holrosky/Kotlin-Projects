package com.example.underground

import android.app.Application
import com.example.underground.di.appModule
import org.koin.android.BuildConfig
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

class UndergroundApplication: Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin{
            androidLogger(
                if (BuildConfig.DEBUG) {
                    Level.DEBUG
                } else {
                    Level.NONE
                }
            )
            androidContext(this@UndergroundApplication)
            modules(appModule)
        }
    }
}