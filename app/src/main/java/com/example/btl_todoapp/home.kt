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
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.DatePicker
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TimePicker
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.Calendar

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"
private const val PREF_NAME = "TodoAppPrefs"
private const val KEY_TASK_LIST = "taskList"
private const val KEY_NOTIFICATIONS_ENABLED = "notifications_enabled"

private lateinit var taskAdapter: TaskAdapter
private lateinit var recyclerView: RecyclerView
private lateinit var sharedPreferences: SharedPreferences
private val taskList = mutableListOf<Task>()
private val filteredTaskList = mutableListOf<Task>()

class home : Fragment() {
    private lateinit var taskAdapter: TaskAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var taskManager: TaskManager
    private val filteredTaskList = mutableListOf<Task>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedPreferences = requireContext().getSharedPreferences("TodoAppPrefs", Context.MODE_PRIVATE)
        taskManager = TaskManager(SharedPreferencesTaskStorage(sharedPreferences))
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)
        val imageButton: ImageButton = view.findViewById(R.id.btnAddTask)
        recyclerView = view.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        filteredTaskList.addAll(taskManager.getAll())
        taskAdapter = TaskAdapter(requireContext(), filteredTaskList,
            onEditClick = { position -> showEditTaskDialog(position) },
            onDeleteClick = { position ->
                val task = filteredTaskList[position]
                taskManager.deleteTask(task)
                filteredTaskList.removeAt(position)
                taskAdapter.notifyItemRemoved(position)
            })

        recyclerView.adapter = taskAdapter
        val etSearch: EditText = view.findViewById(R.id.edtSearchBar)
        etSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: android.text.Editable?) {
                filterTasks(s.toString())
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        imageButton.setOnClickListener { showAddTaskDialog() }
        return view
    }

    private fun filterTasks(query: String) {
        filteredTaskList.clear()
        if (query.isEmpty()) {
            filteredTaskList.addAll(taskManager.getAll())
        } else {
            filteredTaskList.addAll(taskManager.getAll().filter { it.title.contains(query, true) })
        }
        taskAdapter.notifyDataSetChanged()
    }

    private fun showAddTaskDialog() {
        val dialog = DialogFactory.createAddTaskDialog(requireContext()) { title, dueTime ->
            val newTask = TaskFactory.createTask(title, dueTime)
            taskManager.addTask(newTask)
            filterTasks("")
            scheduleNotification(newTask)
        }
        dialog.show()
    }

    private fun showEditTaskDialog(position: Int) {
        val task = filteredTaskList[position]
        val dialog = DialogFactory.createEditTaskDialog(requireContext(), task) { title, dueTime ->
            val updatedTask = TaskFactory.updateTask(task, title, dueTime)
            taskManager.updateTask(task, updatedTask)
            filterTasks("")
            scheduleNotification(updatedTask)
        }
        dialog.show()
    }

    private fun scheduleNotification(task: Task) {
        val notificationsEnabled = sharedPreferences.getBoolean("notifications_enabled", true)
        if (!notificationsEnabled || task.dueTime == null) return

        val alarmManager = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(requireContext(), AlarmReceiver::class.java).apply {
            putExtra("taskTitle", task.title)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            requireContext(), task.hashCode(), intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, task.dueTime!!, pendingIntent)
    }
}