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
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.insees.DialogAddBtnClickListener
import com.example.insees.R
import com.example.insees.Utils.ToDoAdapter
import com.example.insees.Utils.ToDoData
import com.example.insees.databinding.FragmentTodoBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class TodoFragment : Fragment(), DialogAddBtnClickListener,
    ToDoAdapter.ToDoAdapterClicksInterface {

    private lateinit var binding:FragmentTodoBinding
    private lateinit var databaseRef: DatabaseReference
    private lateinit var popUpFragment: PopUpFragment
    private lateinit var adapter:ToDoAdapter
    private lateinit var mList:MutableList<ToDoData>
    private lateinit var auth:FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding= FragmentTodoBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        init()
        getDataFromFirebase()
        registerEvents()

        binding.btnTodoBack.setOnClickListener {
            findNavController().navigate(R.id.action_todoFragment_to_homeFragment)
        }
    }

    private fun getDataFromFirebase() {
        databaseRef.addValueEventListener(object :ValueEventListener{
            @SuppressLint("NotifyDataSetChanged")
            override fun onDataChange(snapshot: DataSnapshot) {
                mList.clear()
                for (taskSnapshot in snapshot.children){
                    val taskTitle=taskSnapshot.child("title").getValue(String::class.java) ?:""
                    val taskDesc=taskSnapshot.child("description").getValue(String::class.java)?:""
                    val taskTime=taskSnapshot.child("time").getValue(String::class.java)?:""
                    val taskDate=taskSnapshot.child("date").getValue(String::class.java)?:""

                    val todoTask=ToDoData(taskTitle,taskDesc,taskTime,taskDate)
                    mList.add(todoTask)

                }
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context,error.message,Toast.LENGTH_SHORT).show()
            }

        })
    }

    private fun registerEvents() {
        binding.btnTodoAddTask.setOnClickListener {
            popUpFragment = PopUpFragment()
            popUpFragment.setListener(this)
            popUpFragment.show(childFragmentManager,
                "PopUpFragment")
        }
    }

    private fun init() {

        auth= FirebaseAuth.getInstance()
        databaseRef=FirebaseDatabase.getInstance().getReference("users")
            .child(auth.currentUser?.uid.toString())
        binding.recyclerView.setHasFixedSize(true)
        binding.recyclerView.layoutManager=LinearLayoutManager(context)
        mList= mutableListOf()
        adapter= ToDoAdapter(mList)
        adapter.setListener(this)
        binding.recyclerView.adapter=adapter
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
        val task = hashMapOf(
            "title" to todoTitle,
            "description" to todoDesc,
            "time" to todoTime,
            "date" to todoDate
        )

        databaseRef.child("Tasks").push().setValue(task).addOnCompleteListener { tasks ->
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
    }

    override fun onDeleteTaskBtnClicked(toDoData: ToDoData) {
        databaseRef
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
                                    if (it.isSuccessful){
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