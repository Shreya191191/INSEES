package com.example.insees.fragment

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
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
    private lateinit var allTasksList: MutableList<ToDoData>
    private var currentTab = "Pending" // "Pending" or "Completed"

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
        allTasksList = mutableListOf()
        adapter = ToDoAdapter(mList)
        binding.recyclerView.adapter = adapter
        setupFilterSortSpinners()
    }

    private fun setupFilterSortSpinners() {
        val filterOptions = arrayOf("All Tasks", "High Priority", "Medium Priority", "Low Priority", "Assignments", "Exams", "Labs", "Projects", "Others")
        val filterAdapter = android.widget.ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, filterOptions)
        filterAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerFilter.adapter = filterAdapter
        binding.spinnerFilter.setSelection(0)

        val sortOptions = arrayOf("Sort: Date & Time", "Sort: Priority (High -> Low)")
        val sortAdapter = android.widget.ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, sortOptions)
        sortAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerSort.adapter = sortAdapter
        binding.spinnerSort.setSelection(0)

        val spinnerListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: View?, position: Int, id: Long) {
                applyFilterAndSort()
            }
            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
        }
        binding.spinnerFilter.onItemSelectedListener = spinnerListener
        binding.spinnerSort.onItemSelectedListener = spinnerListener
    }

    private fun applyFilterAndSort() {
        val selectedFilter = binding.spinnerFilter.selectedItem?.toString() ?: "All Tasks"
        val selectedSort = binding.spinnerSort.selectedItem?.toString() ?: "Sort: Date & Time"

        // 1. Filter
        var filteredList = when (selectedFilter) {
            "High Priority" -> allTasksList.filter { it.priority.lowercase() == "high" }
            "Medium Priority" -> allTasksList.filter { it.priority.lowercase() == "medium" }
            "Low Priority" -> allTasksList.filter { it.priority.lowercase() == "low" }
            "Assignments" -> allTasksList.filter { it.category.lowercase() == "assignment" }
            "Exams" -> allTasksList.filter { it.category.lowercase() == "exam" }
            "Labs" -> allTasksList.filter { it.category.lowercase() == "lab" }
            "Projects" -> allTasksList.filter { it.category.lowercase() == "project" }
            "Others" -> allTasksList.filter { it.category.lowercase() == "other" }
            else -> allTasksList
        }

        // 2. Sort
        filteredList = if (selectedSort == "Sort: Priority (High -> Low)") {
            filteredList.sortedWith(compareBy<ToDoData> {
                when (it.priority.lowercase()) {
                    "high" -> 1
                    "medium" -> 2
                    else -> 3
                }
            }.thenBy { it.taskDate }.thenBy { it.taskTime })
        } else {
            filteredList.sortedWith(compareBy({ it.taskDate }, { it.taskTime }))
        }

        mList.clear()
        mList.addAll(filteredList)
        adapter.notifyDataSetChanged()

        val isEmpty = mList.isEmpty()
        if (currentTab == "Pending") {
            binding.tvNoPendingTasks.text = "🎉 No Pending Tasks"
            binding.tvNoPendingTasks.visibility = if (isEmpty) View.VISIBLE else View.GONE
            binding.recyclerView.visibility = if (isEmpty) View.GONE else View.VISIBLE
        } else {
            binding.tvNoPendingTasks.text = "📭 No Completed Tasks"
            binding.tvNoPendingTasks.visibility = if (isEmpty) View.VISIBLE else View.GONE
            binding.recyclerView.visibility = if (isEmpty) View.GONE else View.VISIBLE
        }
    }

    private fun getDataFromFirebase() {
        TaskManager.fetchTasks(
            currentTab,
            onResult = { tasks ->
                allTasksList.clear()
                allTasksList.addAll(tasks)
                applyFilterAndSort()
            },
            onFailure = {
                Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            }
        )
    }

    private fun registerEvents() {
        // Pending tab click
        binding.tvPending.setOnClickListener {
            if (currentTab != "Pending") {
                currentTab = "Pending"
                updateTabUI()
                getDataFromFirebase()
            }
        }

        // Completed tab click
        binding.tvCompleted.setOnClickListener {
            if (currentTab != "Completed") {
                currentTab = "Completed"
                updateTabUI()
                getDataFromFirebase()
            }
        }

        // Add task button click
        binding.btnTodoAddTask.setOnClickListener {
            popUpFragment = PopUpFragment()
            popUpFragment.setListener(this)
            popUpFragment.show(childFragmentManager, "PopUpFragment")
        }
    }

    private fun updateTabUI() {
        if (currentTab == "Pending") {
            binding.tvPending.setTextColor(Color.WHITE)
            binding.viewPending.visibility = View.VISIBLE

            binding.tvCompleted.setTextColor(Color.GRAY)
            binding.viewCompleted.visibility = View.GONE

            binding.btnTodoAddTask.visibility = View.VISIBLE
        } else {
            binding.tvPending.setTextColor(Color.GRAY)
            binding.viewPending.visibility = View.GONE

            binding.tvCompleted.setTextColor(Color.WHITE)
            binding.viewCompleted.visibility = View.VISIBLE

            binding.btnTodoAddTask.visibility = View.GONE // Hide add button in completed view
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
        todoDateEt: TextView,
        priority: String,
        category: String
    ) {
        if (!TaskManager.isDateTimeValid(todoDate, todoTime)) {
            Toast.makeText(context, "Please select a valid date and time", Toast.LENGTH_SHORT).show()
            return
        }

        TaskManager.saveTask(
            requireContext(),
            todoTitle,
            todoDesc,
            todoDate,
            todoTime,
            priority,
            category,
            onSuccess = {
                Toast.makeText(context, "Todo Saved Successfully", Toast.LENGTH_SHORT).show()
                todoTitleEt.text = null
                todoDescEt.text = null
                todoDateEt.text = null
                todoTimeEt.text = null
                popUpFragment.dismiss()
            },
            onFailure = {
                Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            }
        )
    }

    private fun initSwipe() {
        val swipe = object : Swipe() {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.layoutPosition
                val task = adapter.getItem(position)

                if (currentTab == "Pending") {
                    if (direction == ItemTouchHelper.LEFT) {
                        TaskManager.completeTask(
                            task,
                            onSuccess = {
                                Toast.makeText(context, "Task Completed", Toast.LENGTH_SHORT).show()
                            },
                            onFailure = {
                                Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
                            }
                        )
                    } else if (direction == ItemTouchHelper.RIGHT) {
                        TaskManager.deleteTask(
                            task,
                            onSuccess = {
                                Toast.makeText(context, "Task Deleted", Toast.LENGTH_SHORT).show()
                            },
                            onFailure = {
                                Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                } else {
                    // Completed tasks can only be deleted (swipe right)
                    if (direction == ItemTouchHelper.RIGHT) {
                        TaskManager.deleteTask(
                            task,
                            onSuccess = {
                                Toast.makeText(context, "Task Deleted", Toast.LENGTH_SHORT).show()
                            },
                            onFailure = {
                                Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
                            }
                        )
                    } else {
                        // Re-bind to prevent the swipe from sticking if swiped left
                        adapter.notifyItemChanged(position)
                    }
                }
            }
        }
        val itemTouchHelper = ItemTouchHelper(swipe)
        itemTouchHelper.attachToRecyclerView(binding.recyclerView)
    }
}