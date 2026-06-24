package com.example.insees.fragment

import HomeViewModel
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
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
import com.example.insees.R
import com.example.insees.activity.HomeActivity
import com.example.insees.adapter.HomeToDoAdapter
import com.example.insees.databinding.FragmentHomeBinding
import com.example.insees.model.ToDoData
import com.example.insees.util.DialogAddBtnClickListener
import com.example.insees.util.FirebaseManager
import com.example.insees.util.Swipe
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import com.example.insees.util.TaskManager

class HomeFragment : Fragment(), DialogAddBtnClickListener {

    private lateinit var binding: FragmentHomeBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var viewPager: ViewPager2
    private lateinit var navController: NavController

    private lateinit var homeAdapter: HomeToDoAdapter
    private var tasks: MutableList<ToDoData> = mutableListOf()
    private lateinit var viewModel: HomeViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        init()
    }

    override fun onStart() {
        super.onStart()
        val email = auth.currentUser!!.email.toString()
        viewModel.fetchUserName()
        viewModel.userName.observe(viewLifecycleOwner) {
            var firstName = ""

            for (ch in it) {
                if (ch == ' ')
                    break
                else
                    firstName += ch
            }
            val message = "Hello $firstName!"
            binding.tvHello.text = message

            (activity as HomeActivity).updateNameAndEmail(it, email)
        }
    }

    override fun onResume() {
        super.onResume()
        requireActivity().findViewById<BottomNavigationView>(R.id.bvNavBar).visibility =
            View.VISIBLE
    }

    private fun init() {
        auth = FirebaseManager.getFirebaseAuth()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = findNavController()
        viewPager = requireActivity().findViewById(R.id.viewPager)
        viewPager.currentItem = 0
        setUpViews()

        val uid = auth.currentUser!!.uid

        loadImage("$uid.jpg")

        registerEvents()
        initSwipe()
        fetchDatabase()
    }

    private fun loadImage(localProfileName: String) {
        val localFile = File(requireContext().filesDir, localProfileName)
        if (localFile.exists()) {
            Glide.with(this)
                .load(localFile)
                .placeholder(R.drawable.ic_user_foreground)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(binding.btnProfile)
            (activity as HomeActivity).loadCircleImage(localFile)
        } else {
            viewModel.fetchUserData()
            viewModel.profilePhoto.observe(viewLifecycleOwner) { imageUrl ->
                lifecycleScope.launch(Dispatchers.IO) {
                    try {
                        val bitmap = Glide.with(requireContext())
                            .asBitmap()
                            .load(imageUrl)
                            .submit()
                            .get()
                        //local file me save karo
                        saveImageToLocalFile(bitmap, localFile)
                        withContext(Dispatchers.Main) {
                            binding.btnProfile.setImageBitmap(bitmap)
                            //drawer bhi update karo
                            (activity as HomeActivity).loadCircleImage(localFile)
                        }
                    } catch (e: Exception) {
                        Log.e("PROFILE", "Image download failed", e)
                        withContext(Dispatchers.Main) {
                            Toast.makeText(
                                requireContext(),
                                "Failed to load profile image",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            }
        }

//        Firebase Storage ke sath
//        else{
//            viewModel.fetchUserData()
//            viewModel.profilePhoto.observe(viewLifecycleOwner) {
//                if (it != null){
//                    val photoByteArray = viewModel.profilePhoto.value!!.toByteArray()
//                    val resource = BitmapFactory.decodeByteArray(
//                        photoByteArray,
//                        0,
//                        photoByteArray.size
//                    )
//                    binding.btnProfile.setImageBitmap(resource)
//                    lifecycleScope.launch(Dispatchers.IO){
//                        saveImageToLocalFile(resource, localFile)
//                    }
//                }
//                else Toast.makeText(context, "Image Not Found", Toast.LENGTH_LONG).show()
//            }
//        }
    }

    private fun saveImageToLocalFile(bitmap: Bitmap, file: File) {
        try {
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }
        } catch (e: IOException) {
            Log.e("PROFILE", "Local save failed", e)
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
        TaskManager.fetchTasks(
            "Pending",
            onResult = {
                tasks.clear()
                val upcomingTasks = it.take(2)
                tasks.addAll(upcomingTasks)
                updateRecyclerViewVisibility()
                homeAdapter.notifyDataSetChanged()
            },
            onFailure = {
                Toast.makeText(
                    context,
                    it,
                    Toast.LENGTH_SHORT
                ).show()
            }
        )
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
        if (!TaskManager.isDateTimeValid(todoDate, todoTime)) {
            Toast.makeText(
                context,
                "Please select a valid date and time",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        TaskManager.saveTask(
            requireContext(),
            todoTitle,
            todoDesc,
            todoDate,
            todoTime,
            onSuccess = {
                Toast.makeText(
                    context,
                    "Todo Saved Successfully",
                    Toast.LENGTH_SHORT
                ).show()
                todoTitleEt.text = null
                todoDescEt.text = null
                todoDateEt.text = null
                todoTimeEt.text = null
                updateRecyclerViewVisibility()
            },
            onFailure = {
                Toast.makeText(
                    context,
                    it,
                    Toast.LENGTH_SHORT
                ).show()
            }
        )
    }


    private fun initSwipe() {
        val swipe = object : Swipe() {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.layoutPosition
                val task = homeAdapter.getItem(position)
                if (direction == ItemTouchHelper.LEFT) {
                        TaskManager.completeTask(
                            task,
                            onSuccess = {
                                updateRecyclerViewVisibility()
                                Toast.makeText(
                                    context,
                                    "Task Completed",
                                    Toast.LENGTH_SHORT
                                ).show()
                            },
                            onFailure = {
                                Toast.makeText(
                                    context,
                                    it,
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        )
                } else if (direction == ItemTouchHelper.RIGHT) {
                        TaskManager.deleteTask(
                            task,
                            onSuccess = {
                                Toast.makeText(
                                    context,
                                    "Task Deleted",
                                    Toast.LENGTH_SHORT
                                ).show()
                            },
                            onFailure = {
                                Toast.makeText(
                                    context,
                                    it,
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        )
                }
            }
        }
        val itemTouchHelper = ItemTouchHelper(swipe)
        itemTouchHelper.attachToRecyclerView(binding.rvTodo)
    }

}