package com.example.btl_todoapp

object TaskFactory {
    fun createTask (title : String , dueTime : Long? ): Task {
        return Task (title , false , dueTime)
    }

    fun updateTask(originalTask: Task, newTitle: String, newDueTime: Long?): Task {
        return originalTask.copy(title = newTitle, dueTime = newDueTime)
    }

}