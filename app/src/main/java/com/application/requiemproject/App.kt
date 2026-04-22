package com.application.requiemproject

import android.app.Application
import com.application.requiemproject.data.api.RetrofitClient
import com.application.requiemproject.data.local.db.AppDatabase
import com.application.requiemproject.data.repository.TranslationSettingsRepository
import com.application.requiemproject.managers.SessionManager
import com.application.requiemproject.translator.MyMemoryTranslator

class App: Application() {
    val database by lazy { AppDatabase.getDatabase(this) }
    val sessionManager by lazy { SessionManager(this) }
    val translationSettingsRepository by lazy { TranslationSettingsRepository(this) }

    val myMemoryTranslator by lazy {
        MyMemoryTranslator(
            api = RetrofitClient.api,
            userDao = database.userDao(),
            sessionManager = sessionManager
        )
    }
}
