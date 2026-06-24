package com.example.insees.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.insees.R
import com.example.insees.adapter.ToDoAdapter
import com.example.insees.model.ToDoData
import com.example.insees.util.DialogAddBtnClickListener
import com.example.insees.util.Swipe
import com.example.insees.databinding.FragmentTodoBinding
import com.example.insees.util.TaskManager

class TodoFragment : Fragment(), DialogAddBtnClickListener {

    private lateinit var binding: FragmentTodoBinding
    private lateinit var popUpFragment: PopUpFragment
    private lateinit var adapter: ToDoAdapter
    private lateinit var mList: MutableList<ToDoData>

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
        binding.recyclerView.setHasFixedSize(true)
        binding.recyclerView.layoutManager = LinearLayoutManager(context)
        mList = mutableListOf()
        adapter = ToDoAdapter(mList)
        binding.recyclerView.adapter = adapter
    }

    private fun getDataFromFirebase() {

        TaskManager.fetchTasks(
            "Pending",
            onResult = {
                mList.clear()
                mList.addAll(it)
                adapter.notifyDataSetChanged()
                if (mList.isEmpty()) {
                    binding.tvNoPendingTasks.visibility = View.VISIBLE
                    binding.recyclerView.visibility = View.GONE
                } else {
                    binding.tvNoPendingTasks.visibility = View.GONE
                    binding.recyclerView.visibility = View.VISIBLE
                }
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

    private fun registerEvents() {


        binding.tvCompleted.setOnClickListener {
            requireParentFragment()
                .findNavController()
                .navigate(R.id.completedTaskFragment)
        }

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
                popUpFragment.dismiss()
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
            override fun onSwiped(
                viewHolder: RecyclerView.ViewHolder,
                direction: Int
            ){
                val position = viewHolder.layoutPosition
                val task = adapter.getItem(position)
                if (direction == ItemTouchHelper.LEFT) {
                    TaskManager.completeTask(
                        task,
                        onSuccess = {
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
                }
                else if (direction == ItemTouchHelper.RIGHT) {
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
        itemTouchHelper.attachToRecyclerView(binding.recyclerView)
    }

}