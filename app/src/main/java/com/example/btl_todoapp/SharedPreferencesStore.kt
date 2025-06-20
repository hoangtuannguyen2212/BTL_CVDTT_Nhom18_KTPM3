package com.example.btl_todoapp
import android.content.SharedPreferences



class SharedPreferencesStore(private val prefs: SharedPreferences) : SettingsStore {
    override fun saveBoolean(key: String, value: Boolean) {
        prefs.edit().putBoolean(key, value).apply()
    }

    override fun getBoolean(key: String, default: Boolean): Boolean {
        return prefs.getBoolean(key, default)
    }

    override fun saveString(key: String, value: String) {
        prefs.edit().putString(key, value).apply()
    }

    override fun getString(key: String, default: String): String {
        return prefs.getString(key, default) ?: default
    }
}