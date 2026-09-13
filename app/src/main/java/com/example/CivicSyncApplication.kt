package com.example

import android.app.Application
import android.util.Log

/**
 * Enterprise Application class initializing global exception safety,
 * memory hygiene, and production crash telemetry logging.
 */
class CivicSyncApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        setupGlobalCrashHandler()
    }

    private fun setupGlobalCrashHandler() {
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            // In production release builds, log high-priority diagnostic payload
            // before forwarding to system default or Firebase Crashlytics
            Log.e(
                "CivicSyncGlobal",
                "FATAL UNCAUGHT EXCEPTION in thread [${thread.name}]: ${throwable.localizedMessage}",
                throwable
            )
            defaultHandler?.uncaughtException(thread, throwable)
        }
    }
}
