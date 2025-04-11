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
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
        sharedPreferences = requireContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        loadTasks()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        filteredTaskList.addAll(taskList)

        taskAdapter = TaskAdapter(
            requireContext(),
            filteredTaskList,
            onEditClick = { position -> showEditTaskDialog(position) },
            onDeleteClick = { position ->
                val task = filteredTaskList[position]
                val originalPosition = taskList.indexOf(task)
                if (originalPosition != -1) {
                    taskList.removeAt(originalPosition)
                    filteredTaskList.removeAt(position)
                    taskAdapter.notifyItemRemoved(position)
                    saveTasks()
                }
            }
        )

        val imageButton: ImageButton = view.findViewById(R.id.btnAddTask)
        recyclerView = view.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = taskAdapter

        // Thêm ItemTouchHelper để xử lý vuốt sang phải
        val itemTouchHelperCallback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false // Không hỗ trợ kéo thả
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val task = filteredTaskList[position]
                val originalPosition = taskList.indexOf(task)
                if (originalPosition != -1) {
                    taskList.removeAt(originalPosition)
                    filteredTaskList.removeAt(position)
                    taskAdapter.notifyItemRemoved(position)
                    saveTasks()
                }
            }
        }
        ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(recyclerView)



        // Thiết lập EditText cho tìm kiếm
        val etSearch: EditText = view.findViewById(R.id.edtSearchBar)
        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: android.text.Editable?) {
                filterTasks(s.toString())
            }
        })

        imageButton.setOnClickListener {
            showAddtaskDialog()
        }

        return view
    }

    private fun filterTasks(query: String) {
        filteredTaskList.clear()
        if (query.isEmpty()) {
            filteredTaskList.addAll(taskList)
        } else {
            filteredTaskList.addAll(taskList.filter { task ->
                task.title.contains(query, ignoreCase = true)
            })
        }
        taskAdapter.notifyDataSetChanged()
    }

    private fun showAddtaskDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_task, null)
        val etTaskTitle = dialogView.findViewById<EditText>(R.id.etTaskTitle)
        val dpDueDate = dialogView.findViewById<DatePicker>(R.id.dpDueDate)
        val tpDueTime = dialogView.findViewById<TimePicker>(R.id.tpDueTime)

        val dialog = AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.addTask))
            .setView(dialogView)
            .setPositiveButton(getString(R.string.add)) { _, _ ->
                val taskTitle = etTaskTitle.text.toString().trim()
                if (taskTitle.isNotEmpty()) {
                    val calendar = Calendar.getInstance()
                    calendar.set(dpDueDate.year, dpDueDate.month, dpDueDate.dayOfMonth, tpDueTime.hour, tpDueTime.minute)
                    val dueTime = calendar.timeInMillis
                    val newTask = Task(taskTitle, dueTime = dueTime)
                    taskList.add(newTask)
                    filterTasks("") // Cập nhật danh sách lọc
                    scheduleNotification(newTask)
                    saveTasks()
                    hideKeyboard(dialogView)
                }
            }
            .setNegativeButton(getString(R.string.cancel)) { _, _ ->
                hideKeyboard(dialogView)
            }
            .create()

        dialog.show()
    }

    private fun showEditTaskDialog(position: Int) {
        val task = filteredTaskList[position]
        val originalPosition = taskList.indexOf(task)
        if (originalPosition == -1) return

        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_task, null)
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

        val dialog = AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.editTask))
            .setView(dialogView)
            .setPositiveButton(getString(R.string.save)) { _, _ ->
                val newTitle = etTaskTitle.text.toString().trim()
                if (newTitle.isNotEmpty()) {
                    val calendar = Calendar.getInstance()
                    calendar.set(dpDueDate.year, dpDueDate.month, dpDueDate.dayOfMonth, tpDueTime.hour, tpDueTime.minute)
                    val newDueTime = calendar.timeInMillis
                    val updatedTask = task.copy(title = newTitle, dueTime = newDueTime)
                    taskList[originalPosition] = updatedTask
                    filterTasks("") // Cập nhật danh sách lọc
                    scheduleNotification(updatedTask)
                    saveTasks()
                    hideKeyboard(dialogView)
                }
            }
            .setNegativeButton(getString(R.string.cancel)) { _, _ ->
                hideKeyboard(dialogView)
            }
            .create()

        dialog.show()
    }

    private fun scheduleNotification(task: Task) {
        if (task.dueTime == null) return

        // Kiểm tra trạng thái bật/tắt thông báo
        val notificationsEnabled = sharedPreferences.getBoolean(KEY_NOTIFICATIONS_ENABLED, true)
        if (!notificationsEnabled) return // Không gửi thông báo nếu bị tắt

        val currentTime = System.currentTimeMillis()
        val alarmManager = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(requireContext(), AlarmReceiver::class.java).apply {
            putExtra("taskTitle", task.title)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            requireContext(),
            task.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        if (task.dueTime!! <= currentTime) {
            val receiver = AlarmReceiver()
            receiver.onReceive(requireContext(), intent)
        } else {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, task.dueTime!!, pendingIntent)
        }
    }

    private fun hideKeyboard(view: View) {
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    private fun saveTasks() {
        val editor = sharedPreferences.edit()
        val gson = Gson()
        val json = gson.toJson(taskList)
        editor.putString(KEY_TASK_LIST, json)
        editor.apply()
    }

    private fun loadTasks() {
        val gson = Gson()
        val json = sharedPreferences.getString(KEY_TASK_LIST, null)
        val type = object : TypeToken<MutableList<Task>>() {}.type
        val savedTasks: MutableList<Task>? = gson.fromJson(json, type)
        if (savedTasks != null) {
            taskList.clear()
            taskList.addAll(savedTasks)
            filteredTaskList.clear()
            filteredTaskList.addAll(savedTasks)
            taskList.forEach { scheduleNotification(it) }
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            home().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}