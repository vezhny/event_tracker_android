package com.vezhny.eventtracker

import android.app.Application
import com.vezhny.eventtracker.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class EventTrackerApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@EventTrackerApplication)
            modules(appModule)
        }
    }
}
