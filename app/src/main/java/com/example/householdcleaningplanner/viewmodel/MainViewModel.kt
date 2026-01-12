package com.example.householdcleaningplanner.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.householdcleaningplanner.model.Task
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class MainViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()

    private val _tasks = MutableLiveData<List<Task>>()
    val tasks: LiveData<List<Task>> get() = _tasks

    private val _rooms = MutableLiveData<List<String>>()
    val rooms: LiveData<List<String>> get() = _rooms

    init {
        fetchTasks()
    }

    // 1. Fetch Data (FIXED: MAPS DOCUMENT ID CORRECTLY)
    private fun fetchTasks() {
        db.collection("tasks")
            .orderBy("priority", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.e("ViewModel", "Listen failed.", e)
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    // FIX: We iterate manually to grab the document ID
                    val taskList = snapshot.documents.mapNotNull { doc ->
                        // Convert JSON to Task object
                        val task = doc.toObject(Task::class.java)
                        // FORCE the 'id' field to be the actual Document ID
                        task?.copy(id = doc.id)
                    }

                    _tasks.value = taskList

                    // Extract unique rooms
                    val roomList = taskList.map { it.room }.distinct().sorted()
                    _rooms.value = roomList
                }
                snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Task::class.java)?.copy(id = doc.id)
                }
            }
    }

    // 2. Add Task (No changes needed, Firestore generates ID automatically)
    fun addTask(title: String, room: String, priority: Int) {
        // We create a reference first so we can get the ID
        val newTaskRef = db.collection("tasks").document()
        val task = Task(
            id = newTaskRef.id, // Save ID inside data just in case
            title = title,
            room = room,
            priority = priority,
            isDone = false
        )
        newTaskRef.set(task)
    }

    // 3. Update Task Status (DEBUG VERSION)
    fun updateTaskStatus(taskId: String, isDone: Boolean) {
        if (taskId.isEmpty()) {
            Log.e("CHECKBOX_DEBUG", "Error: Task ID is empty!")
            return
        }

        Log.d("CHECKBOX_DEBUG", "Attempting to update Task $taskId to isDone=$isDone")

        db.collection("tasks").document(taskId)
            .update("isDone", isDone)
            .addOnSuccessListener {
                Log.d("CHECKBOX_DEBUG", "Success: Database updated!")
            }
            .addOnFailureListener { e ->
                Log.e("CHECKBOX_DEBUG", "Failed to update: ${e.message}")
            }
    }

    // 4. Delete Task
    fun deleteTask(taskId: String) {
        if (taskId.isNotEmpty()) {
            db.collection("tasks").document(taskId).delete()
        }
    }

    // 5. Execute Daily Reset (FULL VISIBILITY VERSION)
    fun executeDailyReset(selectedPriorities: List<Int>, onResult: (String) -> Unit) {
        val currentTasks = _tasks.value ?: emptyList()
        val batch = db.batch()
        var matchCount = 0

        // Use a StringBuilder for better logging
        val sb = StringBuilder()
        sb.append("--- STARTING RESET SCAN ---\n")
        sb.append("Target Priorities: $selectedPriorities\n")
        sb.append("Total Tasks Found: ${currentTasks.size}\n")

        currentTasks.forEach { task ->
            // Log EVERY task status
            sb.append("[Task: ${task.title}] -> Priority: ${task.priority}, isDone: ${task.isDone}, ID: ${task.id}\n")

            if (task.isDone && task.priority in selectedPriorities) {
                if (task.id.isNotEmpty()) {
                    val taskRef = db.collection("tasks").document(task.id)
                    batch.update(taskRef, "isDone", false)
                    matchCount++
                    sb.append("   >>> MATCH! Queued for reset.\n")
                } else {
                    sb.append("   >>> ERROR: ID is missing, cannot reset.\n")
                }
            }
        }

        Log.d("RESET_DEBUG", sb.toString()) // Print the full report to Logcat

        if (matchCount > 0) {
            batch.commit()
                .addOnSuccessListener { onResult("Reset $matchCount tasks.") }
                .addOnFailureListener { onResult("Save failed: ${it.message}") }
        } else {
            onResult("No tasks matched. Check Logcat 'RESET_DEBUG' for report.")
        }
    }
}