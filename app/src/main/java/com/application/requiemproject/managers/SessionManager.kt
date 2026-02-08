package com.application.requiemproject.managers

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

class SessionManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(
        "app_session",
        Context.MODE_PRIVATE
    )

    companion object {
        const val KEY_USER_ID = "current_user_id"
    }

    fun saveSession(userId: Long): Unit {
        prefs.edit { putLong(KEY_USER_ID, userId) }
    }

    fun getUserId(): Long {
        return prefs.getLong(KEY_USER_ID, -1L)
    }

    fun clearSession() {
        prefs.edit { remove(KEY_USER_ID).apply() }
    }

    fun isLoggedIn(): Boolean {
        return getUserId() != -1L
    }
}