package com.example.insees.fragment

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.insees.adapter.ToDoAdapter
import com.example.insees.databinding.FragmentCompletedTaskBinding
import com.example.insees.model.ToDoData
import com.example.insees.util.TaskManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.insees.R
import com.example.insees.util.Swipe
import com.google.android.material.bottomnavigation.BottomNavigationView

class CompletedTaskFragment : Fragment() {

    private lateinit var binding: FragmentCompletedTaskBinding
    private lateinit var adapter: ToDoAdapter
    private lateinit var taskList: MutableList<ToDoData>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentCompletedTaskBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        init()
        initSwipe()
        registerEvents()
        loadCompletedTasks()
    }


    private fun registerEvents() {

        binding.tvPending.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.tvPending.setTextColor(Color.GRAY)
        binding.viewPending.visibility = View.GONE

        binding.tvCompleted.setTextColor(Color.WHITE)
        binding.viewCompleted.visibility = View.VISIBLE
    }

    private fun init() {

        taskList = mutableListOf()

        adapter = ToDoAdapter(taskList)

        binding.recyclerView.layoutManager =
            LinearLayoutManager(requireContext())

        binding.recyclerView.setHasFixedSize(true)

        binding.recyclerView.adapter = adapter
    }

    private fun initSwipe() {

        val swipe = object : Swipe(ItemTouchHelper.RIGHT) {
            override fun onSwiped(
                viewHolder: RecyclerView.ViewHolder,
                direction: Int
            ) {
                if (direction == ItemTouchHelper.RIGHT) {
                    val task = adapter.getItem(viewHolder.layoutPosition)
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
        ItemTouchHelper(swipe)
            .attachToRecyclerView(binding.recyclerView)
    }

    private fun loadCompletedTasks() {

        TaskManager.fetchTasks(
            "Completed",
            onResult = {

                taskList.clear()
                taskList.addAll(it)
                adapter.notifyDataSetChanged()

                if (taskList.isEmpty()) {
                    binding.tvNoCompletedTasks.visibility = View.VISIBLE
                    binding.recyclerView.visibility = View.GONE
                } else {
                    binding.tvNoCompletedTasks.visibility = View.GONE
                    binding.recyclerView.visibility = View.VISIBLE
                }
            },
            onFailure = {

                Toast.makeText(
                    requireContext(),
                    it,
                    Toast.LENGTH_SHORT
                ).show()
            }
        )
    }

}