package com.example.insees.Fragments

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.insees.Adapters.ToDoAdapter
import com.example.insees.Dataclasses.ToDoData
import com.example.insees.Utils.DialogAddBtnClickListener
import com.example.insees.Utils.FirebaseManager
import com.example.insees.Utils.Swipe
import com.example.insees.databinding.FragmentTodoBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class TodoFragment : Fragment(), DialogAddBtnClickListener {

    private lateinit var binding: FragmentTodoBinding
    private lateinit var databaseRef: DatabaseReference
    private lateinit var popUpFragment: PopUpFragment
    private lateinit var adapter: ToDoAdapter
    private lateinit var mList: MutableList<ToDoData>
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentTodoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        init()
        initSwipe()
        getDataFromFirebase()
        registerEvents()
    }

    private fun init() {
        auth = FirebaseManager.getFirebaseAuth()
        databaseRef = FirebaseManager.getFirebaseDatabase().reference
        binding.recyclerView.setHasFixedSize(true)
        binding.recyclerView.layoutManager = LinearLayoutManager(context)
        mList = mutableListOf()
        adapter = ToDoAdapter(mList)
        binding.recyclerView.adapter = adapter
    }

    private fun getDataFromFirebase() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val database = databaseRef
                .child("users")
                .child(currentUser.uid)
                .child("Tasks")

            database.addValueEventListener(object : ValueEventListener {
                @SuppressLint("NotifyDataSetChanged")
                override fun onDataChange(snapshot: DataSnapshot) {
                    mList.clear()
                    for (taskSnapshot in snapshot.children) {
                        val taskTitle = taskSnapshot.child("title").getValue(String::class.java) ?: ""
                        val taskDesc = taskSnapshot.child("description").getValue(String::class.java) ?: ""
                        val taskTime = taskSnapshot.child("time").getValue(String::class.java) ?: ""
                        val taskDate = taskSnapshot.child("date").getValue(String::class.java) ?: ""

                        val todoTask = ToDoData(taskTitle, taskDesc, taskTime, taskDate)
                        mList.add(todoTask)
                    }
                    mList.sortWith(compareBy({ it.taskDate }, { it.taskTime }))
                    adapter.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(context, error.message, Toast.LENGTH_SHORT).show()
                }
            })
        } else {
            Toast.makeText(context, "User not authenticated", Toast.LENGTH_SHORT).show()
        }
    }

    private fun registerEvents() {
        binding.btnTodoAddTask.setOnClickListener {
            popUpFragment = PopUpFragment()
            popUpFragment.setListener(this)
            popUpFragment.show(childFragmentManager, "PopUpFragment")
        }
    }

    override fun onSaveTask(
        todoTitle: String,
        todoTitleEt: EditText,
        todoDesc: String,
        todoDescEt: EditText,
        todoTime: String,
        todoTimeEt: TextView,
        todoDate: String,
        todoDateEt: TextView
    ) {
        // Validate task date and time not before current date and time
        if (!isDateTimeValid(todoDate, todoTime)) {
            Toast.makeText(context, "Please select a date and time on or after the current date and time", Toast.LENGTH_SHORT).show()
            return
        }

        val task = hashMapOf(
            "title" to todoTitle,
            "description" to todoDesc,
            "time" to todoTime,
            "date" to todoDate
        )
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val database = databaseRef
                .child("users")
                .child(currentUser.uid)
                .child("Tasks")

            database.push().setValue(task).addOnCompleteListener { tasks ->
                if (tasks.isSuccessful) {
                    Toast.makeText(context, "Todo Saved Successfully", Toast.LENGTH_SHORT).show()
                    todoTitleEt.text = null
                    todoDescEt.text = null
                    todoDateEt.text = null
                    todoTimeEt.text = null
                } else {
                    Toast.makeText(context, tasks.exception.toString(), Toast.LENGTH_SHORT).show()
                }
                popUpFragment.dismiss()
            }
        } else {
            Toast.makeText(context, "User not authenticated", Toast.LENGTH_SHORT).show()
        }
    }

    private fun isDateTimeValid(todoDate: String, todoTime: String): Boolean {
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        val currentDate = Calendar.getInstance().time
        val selectedDateTime = sdf.parse("$todoDate $todoTime") ?: return false
        if(selectedDateTime<currentDate) return false
        else return true
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun initSwipe() {
        val swipe = object : Swipe() {
            @SuppressLint("NotifyDataSetChanged")
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.layoutPosition
                val task = adapter.getItem(position)
                if (direction == ItemTouchHelper.LEFT) {
                    GlobalScope.launch {
                        onSwiped(task)
                    }
                    Toast.makeText(context, "Task Deleted", Toast.LENGTH_SHORT).show()
                } else if (direction == ItemTouchHelper.RIGHT) {
                    GlobalScope.launch {
                        onSwiped(task)
                    }
                    Toast.makeText(context, "Task Finished", Toast.LENGTH_SHORT).show()
                }
                adapter.notifyDataSetChanged()
            }
        }
        val itemTouchHelper = ItemTouchHelper(swipe)
        itemTouchHelper.attachToRecyclerView(binding.recyclerView)
    }

    private fun onSwiped(toDoData: ToDoData) {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val database = databaseRef
                .child("users")
                .child(currentUser.uid)
                .child("Tasks")

            database
                .orderByChild("title")
                .equalTo(toDoData.taskTitle)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        for (taskSnapshot in snapshot.children) {
                            // Check if the found entry matches the data to be deleted
                            if (taskSnapshot.child("title").getValue(String::class.java) == toDoData.taskTitle &&
                                taskSnapshot.child("description").getValue(String::class.java) == toDoData.taskDesc &&
                                taskSnapshot.child("time").getValue(String::class.java) == toDoData.taskTime &&
                                taskSnapshot.child("date").getValue(String::class.java) == toDoData.taskDate) {
                                // Delete the entry
                                taskSnapshot.ref.removeValue()
                                    .addOnCompleteListener {
                                        if (it.isSuccessful) {
                                            Log.d("Delete", "Deleted")
                                        } else {
                                            it.exception?.message?.let { it1 -> Log.d("Failed", it1) }
                                        }
                                    }
                            }
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Toast.makeText(context, error.message, Toast.LENGTH_SHORT).show()
                    }
                })
        } else {
            Toast.makeText(context, "User not authenticated", Toast.LENGTH_SHORT).show()
        }
    }
}