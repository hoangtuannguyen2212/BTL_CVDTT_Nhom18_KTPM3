package com.example.btl_todoapp


import android.util.Log

class LoggingSettingsStore(private val wrapped: SettingsStore) : SettingsStore {
    override fun saveBoolean(key: String, value: Boolean) {
        Log.d("SettingsStore", "Saving boolean: $key = $value")
        wrapped.saveBoolean(key, value)
    }

    override fun getBoolean(key: String, default: Boolean): Boolean {
        return wrapped.getBoolean(key, default)
    }

    override fun saveString(key: String, value: String) {
        Log.d("SettingsStore", "Saving string: $key = $value")
        wrapped.saveString(key, value)
    }

    override fun getString(key: String, default: String): String {
        return wrapped.getString(key, default)
    }
}