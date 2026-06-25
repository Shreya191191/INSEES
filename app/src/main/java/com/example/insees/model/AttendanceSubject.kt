package com.example.insees.model

data class AttendanceSubject(
    val id: String = "",
    val name: String = "",
    @field:JvmField val isLab: Boolean = false,
    val classesAttended: Int = 0,
    val classesScheduled: Int = 0
) {
    // Helper property to calculate attendance percentage
    val attendancePercentage: Float
        get() = if (classesScheduled > 0) {
            (classesAttended.toFloat() / classesScheduled.toFloat()) * 100f
        } else {
            100f
        }
}