package com.example.householdcleaningplanner.model

import com.google.firebase.firestore.PropertyName

data class Task(
    // We keep ID as standard
    var id: String = "",

    val title: String = "",
    val room: String = "",
    val priority: Int = 1,

    // FIX: We force Firebase to map "isDone" correctly
    @get:PropertyName("isDone")
    @set:PropertyName("isDone")
    var isDone: Boolean = false
)