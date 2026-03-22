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
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.insees.R
import com.example.insees.Utils.DialogAddBtnClickListener
import com.example.insees.Utils.FirebaseManager
import com.example.insees.Utils.HomeToDoAdapter
import com.example.insees.Utils.Swipe
import com.example.insees.Utils.ToDoData
import com.example.insees.databinding.FragmentHomeBinding
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class HomeFragment : Fragment(), DialogAddBtnClickListener {

    private lateinit var binding: FragmentHomeBinding
    private lateinit var navController: NavController
    private lateinit var databaseRef : DatabaseReference
    private lateinit var popUpFragment : PopUpFragment
    private lateinit var tasks : MutableList<ToDoData>
    private lateinit var currentUser : FirebaseUser
    private lateinit var homeAdapter: HomeToDoAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)

        setUpViews()
        fetchDatabase()
        registerEvents()
        initSwipe()

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        binding.cardViewStudyMaterials.setOnClickListener{
            navController.navigate(R.id.action_homeFragment_to_semesterFragment)
        }

        binding.btnProfile.setOnClickListener {
            navController.navigate(R.id.action_homeFragment_to_profileFragment)
        }

        binding.cardViewInsees.setOnClickListener{
            navController.navigate(R.id.action_homeFragment_to_inseesAboutInseesFragment)
        }

        binding.cardViewMembers.setOnClickListener{
            navController.navigate(R.id.action_homeFragment_to_aboutMembersFragment)
        }

        binding.btnViewAll.setOnClickListener {
            navController.navigate(R.id.action_homeFragment_to_todoFragment)
        }

        binding.btnAddTask.setOnClickListener{
            navController.navigate(R.id.action_homeFragment_to_popUpFragment)
        }

        return binding.root
    }

    private fun setUpViews() {
        databaseRef = FirebaseManager.getFirebaseDatabase().reference

        currentUser = FirebaseManager.getFirebaseAuth().currentUser!!
        val database = databaseRef.child("users")
        database.child(currentUser.uid).get().addOnSuccessListener {userData->
            if(userData.exists())
            {
                val name = userData.child("name").getValue(String::class.java)
//                val profilePhoto = userData.child("profile_photo").getValue(String::class.java)?.let { Uri.parse(it) }
                var userName = name
                userName = "Hello $userName"
                binding.tvHello.text = userName
//                binding.btnProfile.setImageURI(profilePhoto)
            }
            else
            {
                Toast.makeText(context, "User Doesn't Exist!", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener{
            Toast.makeText(context, "Failed!", Toast.LENGTH_SHORT).show()
        }
        tasks = mutableListOf()
        binding.rvTodo.layoutManager = LinearLayoutManager(context)
        homeAdapter = HomeToDoAdapter(tasks)
        binding.rvTodo.adapter = homeAdapter

        if(tasks.isEmpty())
            binding.rvTodo.visibility = View.GONE
        else
            binding.rvTodo.visibility = View.VISIBLE
    }

    private fun registerEvents() {
        binding.btnAddTask.setOnClickListener {
            popUpFragment = PopUpFragment()
            popUpFragment.setListener(this)
            popUpFragment.show(childFragmentManager,
                "PopUpFragment")
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
        val task = hashMapOf(
            "title" to todoTitle,
            "description" to todoDesc,
            "time" to todoTime,
            "date" to todoDate
        )
        val database  = databaseRef
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
        if(tasks.isEmpty())
            binding.rvTodo.visibility = View.GONE
        else
            binding.rvTodo.visibility = View.VISIBLE
        homeAdapter.notifyDataSetChanged()
    }
    private  fun fetchDatabase() {
        databaseRef = FirebaseDatabase.getInstance().reference
        val query = databaseRef.child("users").child(currentUser.uid).child("Tasks")
        query.addValueEventListener(object : ValueEventListener{
            @SuppressLint("NotifyDataSetChanged")
            override fun onDataChange(snapshot: DataSnapshot) {
                tasks.clear()
                for (taskSnapshot in snapshot.children) {
                    val taskTitle=taskSnapshot.child("title").getValue(String::class.java) ?:""
                    val taskDesc=taskSnapshot.child("description").getValue(String::class.java)?:""
                    val taskTime=taskSnapshot.child("time").getValue(String::class.java)?:""
                    val taskDate=taskSnapshot.child("date").getValue(String::class.java)?:""

                    val todoTask=ToDoData(taskTitle,taskDesc,taskTime,taskDate)
                    tasks.add(todoTask)
                }
                tasks.sortWith(compareBy({
                    it.taskDate }, {it.taskTime}))

                if(tasks.isEmpty())
                    binding.rvTodo.visibility = View.GONE
                else
                    binding.rvTodo.visibility = View.VISIBLE
                homeAdapter.notifyDataSetChanged()
            }
            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireContext(), "Error in Fetching data", Toast.LENGTH_SHORT).show()
            }

        })

    }
    @OptIn(DelicateCoroutinesApi::class)
    private fun initSwipe() {
        val swipe = object : Swipe() {
            @SuppressLint("NotifyDataSetChanged")
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val task = homeAdapter.getItem(position)
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
                homeAdapter.notifyDataSetChanged()
            }
        }
        val itemTouchHelper = ItemTouchHelper(swipe)
        itemTouchHelper.attachToRecyclerView(binding.rvTodo)
    }
    private fun onSwiped(toDoData: ToDoData) {
        val database = databaseRef
            .child("users")
            .child(currentUser.uid)
            .child("Tasks")

        database
            .orderByChild("title")
            .equalTo(toDoData.taskTitle)
            .addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (taskSnapshot in snapshot.children){
                        //check if the found entry matches the data to be deleted
                        if (taskSnapshot.child("title").getValue(String::class.java)==toDoData.taskTitle &&
                            taskSnapshot.child("description").getValue(String::class.java)==toDoData.taskDesc &&
                            taskSnapshot.child("time").getValue(String::class.java)==toDoData.taskTime &&
                            taskSnapshot.child("date").getValue(String::class.java)==toDoData.taskDate){
                            //Delete the entry
                            taskSnapshot.ref.removeValue()
                                .addOnCompleteListener {
                                    if(it.isSuccessful){
                                        Toast.makeText(context,"Deleted Successfully",Toast.LENGTH_SHORT).show()
                                    }else{
                                        Toast.makeText(context,it.exception?.message,Toast.LENGTH_SHORT).show()
                                    }
                                }
                        }
                    }
                }
                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(context,error.message,Toast.LENGTH_SHORT).show()
                }
            })
    }
}