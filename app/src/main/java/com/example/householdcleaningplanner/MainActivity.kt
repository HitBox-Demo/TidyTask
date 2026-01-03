package com.example.householdcleaningplanner

import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.householdcleaningplanner.adapter.TaskAdapter
import com.example.householdcleaningplanner.databinding.ActivityMainBinding
import com.example.householdcleaningplanner.model.Task
import com.example.householdcleaningplanner.viewmodel.MainViewModel

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var taskAdapter: TaskAdapter
    private lateinit var viewModel: MainViewModel

    // Store full list for local filtering
    private var fullTaskList: List<Task> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this)[MainViewModel::class.java]

        setupRecyclerView()
        setupObservers()
        setupButtons()
    }

    private fun setupRecyclerView() {
        // MERGED: Using the new Adapter Constructor
        taskAdapter = TaskAdapter(
            onTaskChecked = { task, isChecked ->
                viewModel.updateTaskStatus(task.id, isChecked)
            },
            onTaskDelete = { task ->
                showDeleteConfirmation(task)
            }
        )

        binding.rvTasks.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = taskAdapter
            // Optional: optimization for fixed size if rows don't change height
            setHasFixedSize(true)
        }
    }

    private fun setupObservers() {
        viewModel.tasks.observe(this) { tasks ->
            fullTaskList = tasks
            // Re-apply filter whenever data changes (e.g., after a Reset)
            val currentRoom = binding.spinnerRooms.selectedItem?.toString() ?: "All Rooms"
            filterTasks(currentRoom)
        }

        viewModel.rooms.observe(this) { rooms ->
            val currentSelection = binding.spinnerRooms.selectedItem?.toString() ?: "All Rooms"
            val roomOptions = listOf("All Rooms") + rooms

            val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, roomOptions)
            binding.spinnerRooms.adapter = adapter

            val index = roomOptions.indexOf(currentSelection)
            if (index >= 0) binding.spinnerRooms.setSelection(index)

            binding.spinnerRooms.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    filterTasks(roomOptions[position])
                }
                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }
        }
    }

    private fun filterTasks(room: String) {
        val filteredList = if (room == "All Rooms") {
            fullTaskList
        } else {
            fullTaskList.filter { it.room == room }
        }

        // Empty State Logic
        if (filteredList.isEmpty()) {
            binding.tvEmptyState.visibility = View.VISIBLE
            binding.rvTasks.visibility = View.GONE
            binding.tvEmptyState.text = if (fullTaskList.isEmpty()) "No tasks yet." else "No tasks in this room."
        } else {
            binding.tvEmptyState.visibility = View.GONE
            binding.rvTasks.visibility = View.VISIBLE
        }

        // MERGED: ListAdapter uses submitList, not updateList
        // We submit a NEW list to force DiffUtil to run
        taskAdapter.submitList(filteredList.toList())
    }

    private fun setupButtons() {
        binding.fabAddTask.setOnClickListener { showAddTaskDialog() }
        binding.btnResetDaily.setOnClickListener { showResetConfirmationDialog() }
    }

    // --- DIALOGS ---

    private fun showAddTaskDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_task, null)
        val etTitle = dialogView.findViewById<EditText>(R.id.etTaskTitle)
        val spRooms = dialogView.findViewById<Spinner>(R.id.spinnerDialogRooms)
        val rgPriority = dialogView.findViewById<RadioGroup>(R.id.rgPriority)

        val existingRooms = viewModel.rooms.value ?: emptyList()
        val defaultRooms = listOf("Kitchen", "Bedroom", "Living Room", "Bathroom", "Office")
        val combinedRooms = (defaultRooms + existingRooms).distinct().sorted()

        spRooms.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, combinedRooms)

        AlertDialog.Builder(this)
            .setTitle("Add New Task")
            .setView(dialogView)
            .setPositiveButton("Add") { _, _ ->
                val title = etTitle.text.toString()
                val selectedRoom = spRooms.selectedItem.toString()
                val priority = when (rgPriority.checkedRadioButtonId) {
                    R.id.rbHigh -> 3
                    R.id.rbMedium -> 2
                    else -> 1
                }
                if (title.isNotEmpty()) {
                    viewModel.addTask(title, selectedRoom, priority)
                } else {
                    Toast.makeText(this, "Title cannot be empty", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showResetConfirmationDialog() {
        val priorities = arrayOf("High", "Medium", "Low")
        val checkedItems = booleanArrayOf(true, true, true) // DEFAULT ALL CHECKED for testing

        AlertDialog.Builder(this)
            .setTitle("Daily Reset")
            .setMultiChoiceItems(priorities, checkedItems) { _, which, isChecked ->
                checkedItems[which] = isChecked
            }
            .setPositiveButton("Reset") { _, _ ->
                val selectedPriorities = mutableListOf<Int>()
                if (checkedItems[0]) selectedPriorities.add(3) // High
                if (checkedItems[1]) selectedPriorities.add(2) // Medium
                if (checkedItems[2]) selectedPriorities.add(1) // Low

                Toast.makeText(this, "Checking tasks...", Toast.LENGTH_SHORT).show()

                viewModel.executeDailyReset(selectedPriorities) { resultMessage ->
                    // Show the exact reason on screen
                    AlertDialog.Builder(this)
                        .setTitle("Reset Result")
                        .setMessage(resultMessage)
                        .setPositiveButton("OK", null)
                        .show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showDeleteConfirmation(task: Task) {
        AlertDialog.Builder(this)
            .setTitle("Delete Task")
            .setMessage("Delete '${task.title}'?")
            .setPositiveButton("Delete") { _, _ -> viewModel.deleteTask(task.id) }
            .setNegativeButton("Cancel", null)
            .show()
    }
}