package com.example.insees.model

data class TimetableSlot(
    val id: String = "",
    val subjectId: String = "",
    val subjectName: String = "",
    val dayOfWeek: String = "", // e.g. "Monday", "Tuesday", etc.
    val time: String = "" // e.g. "09:00 AM - 10:00 AM"
)