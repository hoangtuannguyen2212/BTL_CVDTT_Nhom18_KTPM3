package com.example.btl_todoapp

interface SettingsStore {

    fun saveBoolean(key: String, value: Boolean)
    fun getBoolean(key: String, default: Boolean): Boolean
    fun saveString(key: String, value: String)
    fun getString(key: String, default: String): String
}