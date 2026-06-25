package com.example.insees.util

import android.content.Context
import com.example.insees.model.ToDoData
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

object TaskManager {
    private val auth = FirebaseManager.getFirebaseAuth()

    private fun taskReference(): DatabaseReference {
        val uid = auth.currentUser!!.uid
        return FirebaseManager.getFirebaseDatabase()
            .reference
            .child("users")
            .child(uid)
            .child("Tasks")
    }

    fun saveTask(
        context: Context,
        title: String,
        description: String,
        date: String,
        time: String,
        priority: String,
        category: String,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        val ref = taskReference().push()
        val taskId = ref.key ?: ""
        val task = hashMapOf(
            "id" to taskId,
            "title" to title,
            "description" to description,
            "date" to date,
            "time" to time,
            "status" to "Pending",
            "createdAt" to System.currentTimeMillis(),
            "priority" to priority,
            "category" to category
        )
        ref.setValue(task)
            .addOnSuccessListener {
                ReminderHelper.scheduleReminder(
                    context,
                    title,
                    description,
                    date,
                    time
                )
                onSuccess()
            }
            .addOnFailureListener {
                onFailure(it.message ?: "Unknown Error")
            }
    }



    fun fetchTasks(
        status: String,
        onResult: (MutableList<ToDoData>) -> Unit,
        onFailure: (String) -> Unit
    ) {

        taskReference()
            .addValueEventListener(object : ValueEventListener {

                override fun onDataChange(snapshot: DataSnapshot) {

                    val list = mutableListOf<ToDoData>()

                    for (taskSnapshot in snapshot.children) {

                        val taskId =
                            taskSnapshot.child("id")
                                .getValue(String::class.java)
                                ?: ""

                        val taskTitle =
                            taskSnapshot.child("title")
                                .getValue(String::class.java)
                                ?: ""

                        val taskDesc =
                            taskSnapshot.child("description")
                                .getValue(String::class.java)
                                ?: ""

                        val taskTime =
                            taskSnapshot.child("time")
                                .getValue(String::class.java)
                                ?: ""

                        val taskDate =
                            taskSnapshot.child("date")
                                .getValue(String::class.java)
                                ?: ""

                        val taskStatus =
                            taskSnapshot.child("status")
                                .getValue(String::class.java)
                                ?: "Pending"

                        val createdAt =
                            taskSnapshot.child("createdAt")
                                .getValue(Long::class.java)
                                ?: 0L

                        val priority =
                            taskSnapshot.child("priority")
                                .getValue(String::class.java)
                                ?: "Low"

                        val category =
                            taskSnapshot.child("category")
                                .getValue(String::class.java)
                                ?: "Other"

                        if (taskStatus == status) {
                            list.add(
                                ToDoData(
                                    taskId,
                                    taskTitle,
                                    taskDesc,
                                    taskTime,
                                    taskDate,
                                    taskStatus,
                                    createdAt,
                                    priority,
                                    category
                                )
                            )
                        }
                    }
                    list.sortWith(compareBy({ it.taskDate }, { it.taskTime }))
                    onResult(list)
                }

                override fun onCancelled(error: DatabaseError) {
                    onFailure(error.message)
                }
            })
    }


    fun completeTask(
        task: ToDoData,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        if (task.taskId.isEmpty()) {
            onFailure("Task ID is empty")
            return
        }
        taskReference().child(task.taskId).child("status").setValue("Completed")
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener {
                onFailure(it.message ?: "Error")
            }
    }

    fun deleteTask(
        task: ToDoData,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        if (task.taskId.isEmpty()) {
            onFailure("Task ID is empty")
            return
        }
        taskReference().child(task.taskId).removeValue()
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener {
                onFailure(it.message ?: "Delete Failed")
            }
    }


    fun isDateTimeValid(todoDate: String, todoTime: String): Boolean {
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        val currentDate = Calendar.getInstance().time
        val selectedDateTime = sdf.parse("$todoDate $todoTime") ?: return false
        return selectedDateTime >= currentDate
    }

}