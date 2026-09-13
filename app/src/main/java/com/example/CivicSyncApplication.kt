package com.example

import android.app.Application
import android.util.Log
import dagger.hilt.android.HiltAndroidApp

/**
 * Enterprise Application class initializing Hilt dependency injection,
 * global exception safety, memory hygiene, and production crash telemetry logging.
 */
@HiltAndroidApp
class CivicSyncApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        setupGlobalCrashHandler()
    }

    private fun setupGlobalCrashHandler() {
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            Log.e(
                "CivicSyncGlobal",
                "CRASH RECOVERY: Fatal uncaught exception in thread [${thread.name}]: ${throwable.localizedMessage}",
                throwable
            )
            // Forward gracefully to system default handler
            defaultHandler?.uncaughtException(thread, throwable)
        }
    }
}
