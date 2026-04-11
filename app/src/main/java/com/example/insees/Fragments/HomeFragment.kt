package com.example.insees.Fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.example.insees.Adapters.HomeToDoAdapter
import com.example.insees.Dataclasses.ToDoData
import com.example.insees.R
import com.example.insees.Utils.DialogAddBtnClickListener
import com.example.insees.Utils.FirebaseManager
import com.example.insees.Utils.HomeViewModel
import com.example.insees.Utils.Swipe
import com.example.insees.databinding.FragmentHomeBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class HomeFragment : Fragment(), DialogAddBtnClickListener {

    private lateinit var binding: FragmentHomeBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var viewPager: ViewPager2
    private lateinit var navController: NavController
    private lateinit var databaseRef: DatabaseReference
    private lateinit var currentUser: FirebaseUser
    private lateinit var homeAdapter: HomeToDoAdapter
    private var tasks: MutableList<ToDoData> = mutableListOf()
    private lateinit var viewModel: HomeViewModel
    private var isDataLoaded = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        init()
    }

    override fun onResume() {
        super.onResume()
        requireActivity().findViewById<BottomNavigationView>(R.id.bvNavBar).visibility = View.VISIBLE
    }

    private fun init() {
        databaseRef = FirebaseManager.getFirebaseDatabase().reference
        auth = FirebaseManager.getFirebaseAuth()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = findNavController()
        viewPager = requireActivity().findViewById(R.id.viewPager)
        viewPager.currentItem = 0
        setUpViews()

        viewModel.userName.observe(viewLifecycleOwner) {
            binding.tvHello.text = it
        }

        registerEvents()
        initSwipe()
        fetchDatabase()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        lifecycleScope.coroutineContext.cancel()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(this)[HomeViewModel::class.java]

        viewModel.fetchUserData()

        binding.cardViewStudyMaterials.setOnClickListener {
            viewPager.setCurrentItem(1, false)
        }

        binding.btnProfile.setOnClickListener {
            navController.navigate(R.id.action_viewPagerFragment_to_profileFragment)
        }

        binding.cardViewInsees.setOnClickListener {
            viewPager.setCurrentItem(3, false)
        }

        binding.cardViewMembers.setOnClickListener {
            viewPager.setCurrentItem(3, false)
        }

        binding.btnViewAll.setOnClickListener {
            viewPager.setCurrentItem(2, false)
        }

        binding.btnAddTask.setOnClickListener {
            navController.navigate(R.id.action_viewPagerFragment_to_popUpFragment)
        }
        return binding.root
    }

    private fun setUpViews() {
        binding.rvTodo.layoutManager = LinearLayoutManager(context)
        homeAdapter = HomeToDoAdapter(tasks)
        binding.rvTodo.adapter = homeAdapter
        updateRecyclerViewVisibility()
    }

    private fun updateRecyclerViewVisibility() {
        if (tasks.isEmpty()) {
            binding.rvTodo.visibility = View.GONE
        } else {
            binding.rvTodo.visibility = View.VISIBLE
        }
    }

    private fun registerEvents() {
        binding.btnAddTask.setOnClickListener {
            val popUpFragment = PopUpFragment()
            popUpFragment.setListener(this)
            popUpFragment.show(childFragmentManager, "PopUpFragment")
        }
    }

    private fun fetchDatabase() {
        isDataLoaded = true

        databaseRef.keepSynced(true)
        currentUser = auth.currentUser!!

        lifecycleScope.launch {
            val query = databaseRef.child("users").child(currentUser.uid).child("Tasks")

            query.addValueEventListener(object : ValueEventListener {
                @SuppressLint("NotifyDataSetChanged")
                override fun onDataChange(snapshot: DataSnapshot) {
                    tasks.clear()
                    for (taskSnapshot in snapshot.children) {
                        val taskTitle = taskSnapshot.child("title").getValue(String::class.java) ?: ""
                        val taskDesc = taskSnapshot.child("description").getValue(String::class.java) ?: ""
                        val taskTime = taskSnapshot.child("time").getValue(String::class.java) ?: ""
                        val taskDate = taskSnapshot.child("date").getValue(String::class.java) ?: ""

                        val todoTask = ToDoData(taskTitle, taskDesc, taskTime, taskDate)
                        tasks.add(todoTask)
                    }
                    tasks.sortWith(compareBy({ it.taskDate }, { it.taskTime }))

                    updateRecyclerViewVisibility()
                    homeAdapter.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {
                    lifecycleScope.launch(Dispatchers.Main) {
                        Toast.makeText(requireContext(), "Error in Fetching data", Toast.LENGTH_SHORT).show()
                    }
                }
            })
        }
    }

    @SuppressLint("NotifyDataSetChanged")
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
        if (!isDateValid(todoDate, todoTime)) {
            Toast.makeText(context, "Please select a date and time on or after the current date and time", Toast.LENGTH_SHORT).show()
            return
        }

        val task = hashMapOf(
            "title" to todoTitle,
            "description" to todoDesc,
            "time" to todoTime,
            "date" to todoDate
        )
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
            PopUpFragment().dismiss()
        }
        updateRecyclerViewVisibility()
    }

    private fun isDateValid(todoDate: String, todoTime: String): Boolean {
        val sdfDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val sdfTime = SimpleDateFormat("HH:mm", Locale.getDefault())

        val currentDate = Calendar.getInstance().time
        val selectedDate = sdfDate.parse(todoDate) ?: return false
        val selectedTime = sdfTime.parse(todoTime) ?: return false

        // Combine date and time into one datetime object
        val selectedDateTime = Calendar.getInstance().apply {
            time = selectedDate
            val time = Calendar.getInstance()
            time.time = selectedTime
            set(Calendar.HOUR_OF_DAY, time.get(Calendar.HOUR_OF_DAY))
            set(Calendar.MINUTE, time.get(Calendar.MINUTE))
        }.time

        return selectedDateTime >= currentDate
    }

    private fun initSwipe() {
        val swipe = object : Swipe() {
            @SuppressLint("NotifyDataSetChanged")
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.layoutPosition
                val task = homeAdapter.getItem(position)
                if (direction == ItemTouchHelper.LEFT) {
                    lifecycleScope.launch {
                        onSwiped(task, position)
                        withContext(Dispatchers.Main) {
                            Toast.makeText(context, "Task Deleted", Toast.LENGTH_SHORT).show()
                            homeAdapter.notifyItemRemoved(position)
                        }
                    }
                } else if (direction == ItemTouchHelper.RIGHT) {
                    lifecycleScope.launch {
                        onSwiped(task, position)
                        withContext(Dispatchers.Main) {
                            Toast.makeText(context, "Task Finished", Toast.LENGTH_SHORT).show()
                            homeAdapter.notifyItemRemoved(position)
                        }
                    }
                }
            }
        }
        val itemTouchHelper = ItemTouchHelper(swipe)
        itemTouchHelper.attachToRecyclerView(binding.rvTodo)
    }

    private fun onSwiped(toDoData: ToDoData, position: Int) {
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
                            taskSnapshot.child("date").getValue(String::class.java) == toDoData.taskDate
                        ) {
                            taskSnapshot.ref.removeValue()
                            tasks.remove(toDoData)
                            break
                        }
                    }
                    updateRecyclerViewVisibility()
                    homeAdapter.notifyItemRemoved(position)
                }

                override fun onCancelled(error: DatabaseError) {
                    lifecycleScope.launch(Dispatchers.Main) {
                        Toast.makeText(requireContext(), "Error in Deleting Task", Toast.LENGTH_SHORT).show()
                    }
                }
            })
    }
}