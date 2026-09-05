package com.example

import android.app.Application
import com.example.data.local.db.ServexaDatabase

class ServexaApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Initialize room db singleton on startup
        ServexaDatabase.getInstance(this)
    }
}
