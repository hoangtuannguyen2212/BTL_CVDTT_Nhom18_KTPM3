package com.example.btl_todoapp

interface TaskStorage {
    fun saveTasks(tasks: List<Task>)
    fun loadTasks(): List<Task>
}
