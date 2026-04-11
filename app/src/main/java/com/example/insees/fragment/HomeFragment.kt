package com.example.insees.fragment

import HomeViewModel
import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.BitmapFactory
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
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.insees.activity.HomeActivity
import com.example.insees.adapter.HomeToDoAdapter
import com.example.insees.model.ToDoData
import com.example.insees.R
import com.example.insees.util.DialogAddBtnClickListener
import com.example.insees.util.FirebaseManager
import com.example.insees.util.Swipe
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
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class HomeFragment : Fragment(), DialogAddBtnClickListener{

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

        viewModel.fetchUserName()

        val email = auth.currentUser!!.email.toString()

        viewModel.userName.observe(viewLifecycleOwner) {
            var firstName = ""

            for(ch in it){
                if(ch == ' ')
                    break
                else
                    firstName += ch
            }
            val message = "Hello $firstName!"
            binding.tvHello.text = message

            (activity as HomeActivity).updateNameAndEmail(it, email)
        }

        val uid = auth.currentUser!!.uid

        loadImage("$uid.jpg")

        registerEvents()
        initSwipe()
        fetchDatabase()
    }

    private fun loadImage(localProfileName: String) {
        val localFile = File(requireContext().filesDir, localProfileName)
        if(localFile.exists()){
            Glide.with(this)
                .load(localFile)
                .placeholder(R.drawable.ic_user_foreground)
                .diskCacheStrategy(DiskCacheStrategy.ALL) // Enable disk caching
                .into(binding.btnProfile)
            (activity as HomeActivity).loadCircleImage(localFile)
        }
        else{
            viewModel.fetchUserData()
            viewModel.profilePhoto.observe(viewLifecycleOwner) {
                if (it != null){
                    val photoByteArray = viewModel.profilePhoto.value!!.toByteArray()
                    val resource = BitmapFactory.decodeByteArray(
                        photoByteArray,
                        0,
                        photoByteArray.size
                    )
                    binding.btnProfile.setImageBitmap(resource)
                    lifecycleScope.launch(Dispatchers.IO){
                        saveImageToLocalFile(resource, localFile)
                    }
                }
                else Toast.makeText(context, "Image Not Found", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun saveImageToLocalFile(bitmap: Bitmap, file: File) {
        try {
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 50, out)
            }
        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(context, "Failed to save image locally", Toast.LENGTH_SHORT).show()
        }
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

        binding.cardViewStudyMaterials.setOnClickListener {
            viewPager.setCurrentItem(1, false)
        }

        binding.btnProfile.setOnClickListener {
            (activity as HomeActivity).toggleDrawer()
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
//            PopUpFragment().dismiss()
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