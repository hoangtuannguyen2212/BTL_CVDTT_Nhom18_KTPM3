package com.example.btl_todoapp


import com.example.btl_todoapp.Task

class TaskManager(private val storage: TaskStorage) {
    private val tasks = mutableListOf<Task>()

    init {
        tasks.addAll(storage.loadTasks())
    }

    fun addTask(task: Task) {
        tasks.add(task)
        storage.saveTasks(tasks)
    }

    fun deleteTask(task: Task) {
        tasks.remove(task)
        storage.saveTasks(tasks)
    }

    fun updateTask(old: Task, updated: Task) {
        val index = tasks.indexOf(old)
        if (index != -1) {
            tasks[index] = updated
            storage.saveTasks(tasks)
        }
    }

    fun getAll(): List<Task> = tasks.toList()
}