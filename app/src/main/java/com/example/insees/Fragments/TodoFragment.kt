package com.example.insees.Fragments

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
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
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.insees.Adapters.ToDoAdapter
import com.example.insees.Dataclasses.ToDoData
import com.example.insees.Utils.DialogAddBtnClickListener
import com.example.insees.Utils.FirebaseManager
import com.example.insees.Utils.NotificationReceiver
import com.example.insees.Utils.Swipe
import com.example.insees.databinding.FragmentTodoBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class TodoFragment : Fragment(), DialogAddBtnClickListener {

    private lateinit var binding: FragmentTodoBinding
    //    private lateinit var viewPager: ViewPager2
    private lateinit var databaseRef : DatabaseReference
    private lateinit var popUpFragment: PopUpFragment
    private lateinit var adapter: ToDoAdapter
    private lateinit var mList: MutableList<ToDoData>
    private lateinit var auth: FirebaseAuth
    private lateinit var alarmManager: AlarmManager
    private lateinit var pendingIntent: PendingIntent

    private val PERMISSION_REQUEST_CODE = 101

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentTodoBinding.inflate(inflater, container, false)
//        viewPager = requireActivity().findViewById<ViewPager2>(R.id.viewPager)
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

        createNotificationChannel()

        // Initialize AlarmManager and PendingIntent
        alarmManager = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, NotificationReceiver::class.java)
        pendingIntent = PendingIntent.getBroadcast(
            context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Request SCHEDULE_EXACT_ALARM permission if necessary
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!hasScheduleExactAlarmPermission()) {
                requestScheduleExactAlarmPermission()
            }
        }

        // Start periodic checks for notifications
        checkAndScheduleNotifications()
    }


    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Task Reminder Channel"
            val descriptionText = "Channel for Task Reminders"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel("task_reminder_channel", name, importance)
                .apply {
                    description = descriptionText
                }
            val notificationManager: NotificationManager = requireContext().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private fun hasScheduleExactAlarmPermission(): Boolean {
        return ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.SCHEDULE_EXACT_ALARM) == PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.USE_EXACT_ALARM) == PackageManager.PERMISSION_GRANTED
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private fun requestScheduleExactAlarmPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), Manifest.permission.SCHEDULE_EXACT_ALARM) ||
            ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), Manifest.permission.USE_EXACT_ALARM)) {
            // Show an explanation to the user *asynchronously* -- don't block
            // this thread waiting for the user's response! After the user
            // sees the explanation, try again to request the permission.
            Toast.makeText(context, "Exact Alarm Permission is required to schedule notifications", Toast.LENGTH_SHORT).show()
        } else {
            // No explanation needed; request the permission
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.SCHEDULE_EXACT_ALARM, Manifest.permission.USE_EXACT_ALARM), PERMISSION_REQUEST_CODE)
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                // Permission granted
                Toast.makeText(context, "Exact Alarm Permission Granted", Toast.LENGTH_SHORT).show()
            } else {
                // Permission denied
                Toast.makeText(context, "Exact Alarm Permission Denied", Toast.LENGTH_SHORT).show()
            }
        }
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
        // Validate task date not before current date
        if (!isDateValid(todoDate)) {
            Toast.makeText(context, "Please select a date on or after today", Toast.LENGTH_SHORT).show()
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

                    // Schedule the notification
                    scheduleNotification(todoTitle, todoDesc, todoDate, todoTime)
                } else {
                    Toast.makeText(context, tasks.exception.toString(), Toast.LENGTH_SHORT).show()
                }
                popUpFragment.dismiss()
            }
        } else {
            Toast.makeText(context, "User not authenticated", Toast.LENGTH_SHORT).show()
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun checkAndScheduleNotifications() {
        GlobalScope.launch {
            while (true) {
                // Fetch tasks from Firebase
                getDataFromFirebase()

                // Check for tasks whose time matches the current time
                mList.forEach { task ->
                    val currentTime = Calendar.getInstance()
                    val taskTime = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                        .parse("${task.taskDate} ${task.taskTime}")

                    if (taskTime != null && currentTime.time >= taskTime) {
                        // Schedule notification for this task
                        task.let {
                            scheduleNotification(it.taskTitle, it.taskDesc, it.taskDate, it.taskTime)
                        }
                    }
                }

                // Sleep for 1 minute before checking again
                delay(60000) // Check every 1 minute
            }
        }
    }

    @SuppressLint("ScheduleExactAlarm")
    private fun scheduleNotification(title: String, description: String, date: String, time: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !hasScheduleExactAlarmPermission()) {
            requestScheduleExactAlarmPermission()
            return
        }

        val intent = Intent(context, NotificationReceiver::class.java).apply {
            putExtra("taskTitle", title)
            putExtra("taskDesc", description)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        val dateTime = "$date $time"

        val triggerAtMillis = sdf.parse(dateTime)?.time
        if (triggerAtMillis == null) {
            Log.e("scheduleNotification", "Failed to parse date and time: $dateTime")
            return
        }

        Log.d("scheduleNotification", "Scheduling notification at $triggerAtMillis ($dateTime)")

        alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
        Log.d("scheduleNotification", "Notification scheduled with AlarmManager")
    }


    private fun isDateValid(todoDate: String): Boolean {
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val currentDate = Calendar.getInstance()
        val selectedDate = Calendar.getInstance().apply {
            time = sdf.parse(todoDate) ?: return false
        }

        return !selectedDate.before(currentDate) &&
                (selectedDate.get(Calendar.MONTH) >= currentDate.get(Calendar.MONTH) ||
                        selectedDate.get(Calendar.YEAR) >= currentDate.get(Calendar.YEAR))
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