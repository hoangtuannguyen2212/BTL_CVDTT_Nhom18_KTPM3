package com.example.btl_todoapp

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.widget.DatePicker
import android.widget.EditText
import android.widget.TimePicker
import androidx.appcompat.app.AlertDialog

import java.util.Calendar

object DialogFactory {
    fun createAddTaskDialog(context: Context, onAdd: (String, Long) -> Unit): AlertDialog {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_task, null)
        val etTaskTitle = dialogView.findViewById<EditText>(R.id.etTaskTitle)
        val dpDueDate = dialogView.findViewById<DatePicker>(R.id.dpDueDate)
        val tpDueTime = dialogView.findViewById<TimePicker>(R.id.tpDueTime)

        return AlertDialog.Builder(context)
            .setTitle(context.getString(R.string.addTask))
            .setView(dialogView)
            .setPositiveButton(context.getString(R.string.add)) { _, _ ->
                val taskTitle = etTaskTitle.text.toString().trim()
                if (taskTitle.isNotEmpty()) {
                    val calendar = Calendar.getInstance()
                    calendar.set(dpDueDate.year, dpDueDate.month, dpDueDate.dayOfMonth, tpDueTime.hour, tpDueTime.minute)
                    val dueTime = calendar.timeInMillis
                    onAdd(taskTitle, dueTime)
                }
            }
            .setNegativeButton(context.getString(R.string.cancel), null)
            .create()
    }


    fun createEditTaskDialog(context: Context, task: Task, onEdit: (String, Long) -> Unit): AlertDialog {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_task, null)
        val etTaskTitle = dialogView.findViewById<EditText>(R.id.etTaskTitle)
        val dpDueDate = dialogView.findViewById<DatePicker>(R.id.dpDueDate)
        val tpDueTime = dialogView.findViewById<TimePicker>(R.id.tpDueTime)

        etTaskTitle.setText(task.title)
        task.dueTime?.let { dueTime ->
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = dueTime
            dpDueDate.updateDate(
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            )
            tpDueTime.hour = calendar.get(Calendar.HOUR_OF_DAY)
            tpDueTime.minute = calendar.get(Calendar.MINUTE)
        }

        return AlertDialog.Builder(context)
            .setTitle(context.getString(R.string.editTask))
            .setView(dialogView)
            .setPositiveButton(context.getString(R.string.save)) { _, _ ->
                val newTitle = etTaskTitle.text.toString().trim()
                if (newTitle.isNotEmpty()) {
                    val calendar = Calendar.getInstance()
                    calendar.set(dpDueDate.year, dpDueDate.month, dpDueDate.dayOfMonth, tpDueTime.hour, tpDueTime.minute)
                    val newDueTime = calendar.timeInMillis
                    onEdit(newTitle, newDueTime)
                }
            }
            .setNegativeButton(context.getString(R.string.cancel), null)
            .create()
    }


}