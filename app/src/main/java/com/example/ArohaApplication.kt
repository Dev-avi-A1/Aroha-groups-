package com.example

import android.app.Application
import com.example.data.local.database.ArohaDatabase
import com.example.data.repository.ArohaRepository

class ArohaApplication : Application() {

    val database: ArohaDatabase by lazy {
        ArohaDatabase.getInstance(this)
    }

    val repository: ArohaRepository by lazy {
        ArohaRepository(database.arohaDao())
    }

    override fun onCreate() {
        super.onCreate()
    }
}
