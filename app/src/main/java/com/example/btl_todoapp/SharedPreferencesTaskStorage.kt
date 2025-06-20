package com.example.btl_todoapp

import android.content.SharedPreferences
import com.example.btl_todoapp.Task
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class SharedPreferencesTaskStorage(private val prefs: SharedPreferences) : TaskStorage {
    private val gson = Gson()
    override fun saveTasks(tasks: List<Task>) {
        val json = gson.toJson(tasks)
        prefs.edit().putString("taskList", json).apply()
    }

    override fun loadTasks(): List<Task> {
        val json = prefs.getString("taskList", null)
        val type = object : TypeToken<List<Task>>() {}.type
        return gson.fromJson(json, type) ?: emptyList()
    }
}