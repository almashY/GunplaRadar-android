package com.example.gunplaradar

import android.app.Application
import com.example.gunplaradar.data.database.AppDatabase

class GunplaRadarApplication : Application() {
    val database: AppDatabase by lazy {
        AppDatabase.getDatabase(this)
    }
}
