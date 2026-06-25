package com.example.insees.model

data class ToDoData(var taskId: String = "",
                    var taskTitle:String = "",
                    var taskDesc:String = "",
                    var taskTime:String = "",
                    var taskDate:String = "",
                    var status: String = "Pending",
                    var createdAt: Long = System.currentTimeMillis(),
                    var priority: String = "Low",
                    var category: String = "Other"
)