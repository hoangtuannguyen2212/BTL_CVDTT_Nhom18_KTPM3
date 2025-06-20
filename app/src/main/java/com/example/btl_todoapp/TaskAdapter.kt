package com.example.btl_todoapp

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.view.menu.MenuView.ItemView
import androidx.constraintlayout.motion.widget.MotionScene.Transition.TransitionOnClick
import androidx.recyclerview.widget.RecyclerView
import org.w3c.dom.Text
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class TaskAdapter(private val context: Context,private  val tasks : MutableList<Task> ,
                  private val onEditClick: (position : Int) -> Unit ,
    private val onDeleteClick: (position: Int) -> Unit): RecyclerView.Adapter<TaskAdapter.TaskViewHolder>() {

    private val handler = Handler(Looper.getMainLooper())

    private val updateRunnable = object : Runnable {
        override fun run() {
            notifyDataSetChanged() // Cập nhật toàn bộ danh sách để làm mới thời gian còn lại
            handler.postDelayed(this, 1000) // Cập nhật mỗi 1 s
        }
    }


    inner class TaskViewHolder (itemView: View): RecyclerView.ViewHolder(itemView){
        val cbTask: CheckBox = itemView.findViewById(R.id.cbTask)
        val btnEdit:  ImageButton = itemView.findViewById(R.id.btnEditTask)
        val btnDelete: ImageButton = itemView.findViewById(R.id.btnDelete)
        val tvDueTime : TextView = itemView.findViewById(R.id.tvDueTime)
        val tvTimeRemaining: TextView = itemView.findViewById(R.id.tvTimeRemaining)

        init {
            // Thêm sự kiện nhấn giữ để chỉnh sửa
            itemView.setOnLongClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onEditClick(position)
                    true
                } else {
                    false
                }
            }
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskAdapter.TaskViewHolder {

        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_task , parent , false)
        return TaskViewHolder(view)
    }

    override fun onBindViewHolder(holder: TaskAdapter.TaskViewHolder, position: Int) {

        val task = tasks[position]
        holder.cbTask.text = task.title
        holder.cbTask.isChecked = task.isDone

        // Hiển thị thời gian đến hạn
        task.dueTime?.let { dueTime ->
            val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            holder.tvDueTime.text = dateFormat.format(Date(dueTime))

            // Tính toán và hiển thị thời gian còn lại
            val currentTime = System.currentTimeMillis()
            val timeRemaining = dueTime - currentTime
            if (timeRemaining > 0) {
                holder.tvTimeRemaining.text = "${context.getString(R.string.remainingTime)} ${formatTimeRemaining(timeRemaining)}"
            } else {
                holder.tvTimeRemaining.text = "${context.getString(R.string.overdue)}"
            }
        } ?: run {
            holder.tvDueTime.text = "Không có thời hạn"
            holder.tvTimeRemaining.text = "Còn lại: --"
        }


        // Hiển thị thời gian
        task.dueTime?.let { dueTime ->
            val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            holder.tvDueTime.text = dateFormat.format(Date(dueTime))
        } ?: run {
            holder.tvDueTime.text = "Không có thời hạn"
        }

        // cap nhat trang thai khi checkbox duoc thay doi
        holder.cbTask.setOnCheckedChangeListener{_, isChecked ->
            task.isDone = isChecked
        }

        //xu ly su kien cho nut editTask
        holder.btnEdit.setOnClickListener{
            val currentPosition = holder.adapterPosition
            if(currentPosition != RecyclerView.NO_POSITION){
                onEditClick(currentPosition)
            }

        }
        //xu ly su kien cho nut Delete
        holder.btnDelete.setOnClickListener{
            val currentPosition = holder.adapterPosition
            if(currentPosition != RecyclerView.NO_POSITION){
                onDeleteClick(currentPosition)
            }
        }
    }

    override fun getItemCount(): Int  = tasks.size

    //ham them task moi
    fun addTask(task: Task ){
        tasks.add(task )
        notifyItemInserted(tasks.size - 1)
    }
    //ham cap nhat task tai vi tri cu the
    fun updateTask(position: Int, newTask: Task) {
        if (position >= 0 && position < tasks.size) {
            tasks[position] = newTask
            notifyItemChanged(position)
        }
    }
    // ham xoa task
    fun removeTask(position: Int) {
        if (position >= 0 && position < tasks.size) {
            val task = tasks[position] // Lấy task trước khi xóa
            tasks.removeAt(position)
            notifyItemRemoved(position)
            cancelNotification(task) // Hủy thông báo với task
        }
    }

    private fun cancelNotification(task: Task) {
        if (task.dueTime == null) return // Không hủy nếu không có thời gian
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            task.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }

    // Hàm định dạng thời gian còn lại
    private fun formatTimeRemaining(timeInMillis: Long): String {
        val seconds = timeInMillis / 1000
        val minutes = seconds / 60
        val hours = minutes / 60
        val days = hours / 24

        return when {
            days > 0 -> "$days ${context.getString(R.string.day)} ${hours % 24} ${context.getString(R.string.hour)}"
            hours > 0 -> "$hours ${context.getString(R.string.hour)} ${minutes % 60} ${context.getString(R.string.minute)}"
            minutes > 0 -> "$minutes ${context.getString(R.string.minute)}"
            else -> "$seconds ${context.getString(R.string.second)}"
        }
    }


    // Bắt đầu cập nhật thời gian khi Adapter được gắn vào RecyclerView
    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        handler.post(updateRunnable)
    }

    // Dừng cập nhật khi Adapter bị gỡ khỏi RecyclerView
    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        handler.removeCallbacks(updateRunnable)
    }

}
