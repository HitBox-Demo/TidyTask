package com.example.householdcleaningplanner.adapter

import android.graphics.Color
import android.graphics.Paint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.householdcleaningplanner.databinding.ItemTaskBinding
import com.example.householdcleaningplanner.model.Task

// MERGED: Using ListAdapter (New) which fixes the "uncheck all" issue
class TaskAdapter(
    private val onTaskChecked: (Task, Boolean) -> Unit,
    private val onTaskDelete: (Task) -> Unit
) : ListAdapter<Task, TaskAdapter.TaskViewHolder>(DIFF_CALLBACK) {

    inner class TaskViewHolder(val binding: ItemTaskBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val binding = ItemTaskBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TaskViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val task = getItem(position)

        with(holder.binding) {
            tvTaskTitle.text = task.title
            tvRoomName.text = task.room

            // 1. Detach listener to prevent infinite loops during binding
            cbTaskDone.setOnCheckedChangeListener(null)

            // 2. Set state
            cbTaskDone.isChecked = task.isDone

            // Priority Logic
            val colorCode = when (task.priority) {
                3 -> "#D32F2F"      // High: Red
                2 -> "#FBC02D"      // Med: Yellow
                else -> "#388E3C"   // Low: Green
            }
            viewPriorityStrip.setBackgroundColor(Color.parseColor(colorCode))

            // Strikethrough Logic
            if (task.isDone) {
                tvTaskTitle.paintFlags = tvTaskTitle.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                tvTaskTitle.alpha = 0.5f
            } else {
                tvTaskTitle.paintFlags = tvTaskTitle.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
                tvTaskTitle.alpha = 1.0f
            }

            // 3. Re-attach listener
            cbTaskDone.setOnCheckedChangeListener { _, isChecked ->
                onTaskChecked(task, isChecked)
            }

            btnDelete.setOnClickListener {
                onTaskDelete(task)
            }
        }
    }

    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Task>() {
            override fun areItemsTheSame(oldItem: Task, newItem: Task): Boolean {
                // Compares IDs. If IDs are different, it's a different row.
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: Task, newItem: Task): Boolean {
                // Compares content. If true, no redraw needed. If false, redraws the row.
                return oldItem == newItem
            }
        }
    }
}