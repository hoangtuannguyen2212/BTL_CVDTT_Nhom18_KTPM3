package com.example.btl_todoapp

data class Task (
    val title: String,
    var isDone: Boolean = false,
    var dueTime: Long? = null
)