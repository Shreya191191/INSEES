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
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {

        val task = hashMapOf(
            "title" to title,
            "description" to description,
            "date" to date,
            "time" to time,
            "status" to "Pending",
            "createdAt" to System.currentTimeMillis()
        )
        taskReference()
            .push()
            .setValue(task)
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

                        if (taskStatus == status) {
                            list.add(
                                ToDoData(
                                    taskTitle,
                                    taskDesc,
                                    taskTime,
                                    taskDate,
                                    taskStatus,
                                    createdAt
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
        taskReference()
            .orderByChild("title")
            .equalTo(task.taskTitle)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (taskSnapshot in snapshot.children) {
                        if (
                            taskSnapshot.child("title").getValue(String::class.java) == task.taskTitle &&
                            taskSnapshot.child("description").getValue(String::class.java) == task.taskDesc &&
                            taskSnapshot.child("time").getValue(String::class.java) == task.taskTime &&
                            taskSnapshot.child("date").getValue(String::class.java) == task.taskDate
                        ) {
                            taskSnapshot.ref
                                .child("status")
                                .setValue("Completed")
                                .addOnSuccessListener {
                                    onSuccess()
                                }
                                .addOnFailureListener {
                                    onFailure(it.message ?: "Error")
                                }
                            break
                        }
                    }
                }
                override fun onCancelled(error: DatabaseError) {
                    onFailure(error.message)
                }
            })
    }

    fun deleteTask(
        task: ToDoData,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        taskReference()
            .orderByChild("title")
            .equalTo(task.taskTitle)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (taskSnapshot in snapshot.children) {
                        if (
                            taskSnapshot.child("title")
                                .getValue(String::class.java) == task.taskTitle &&
                            taskSnapshot.child("description")
                                .getValue(String::class.java) == task.taskDesc &&
                            taskSnapshot.child("time")
                                .getValue(String::class.java) == task.taskTime &&
                            taskSnapshot.child("date").getValue(String::class.java) == task.taskDate
                        ) {
                            taskSnapshot.ref.removeValue()
                                .addOnSuccessListener {
                                    onSuccess()
                                }
                                .addOnFailureListener {
                                    onFailure(it.message ?: "Delete Failed")
                                }
                            break
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    onFailure(error.message)
                }
            })
    }


    fun isDateTimeValid(todoDate: String, todoTime: String): Boolean {
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        val currentDate = Calendar.getInstance().time
        val selectedDateTime = sdf.parse("$todoDate $todoTime") ?: return false
        return selectedDateTime >= currentDate
    }

}