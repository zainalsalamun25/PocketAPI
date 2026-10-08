package com.example.pocketapi

import android.app.Application
import com.example.pocketapi.data.local.AppDatabase
import com.example.pocketapi.data.network.HttpEngine
import com.example.pocketapi.data.repository.ApiRepository

class PocketApiApp : Application() {
    lateinit var repository: ApiRepository
        private set

    override fun onCreate() {
        super.onCreate()
        val database = AppDatabase.getDatabase(this)
        val httpEngine = HttpEngine()
        repository = ApiRepository(httpEngine, database.requestHistoryDao())
    }
}