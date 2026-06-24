package com.example.insees.model

data class ToDoData(var taskTitle:String = "",
                    var taskDesc:String = "",
                    var taskTime:String = "",
                    var taskDate:String = "",
                    var status: String = "Pending",
                    var createdAt: Long = System.currentTimeMillis()
)