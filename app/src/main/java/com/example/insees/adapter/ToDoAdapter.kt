package com.example.insees.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.insees.R
import com.example.insees.model.ToDoData
import com.example.insees.databinding.TaskDescriptionBinding

class ToDoAdapter(private val list: MutableList<ToDoData>)
    : RecyclerView.Adapter<ToDoAdapter.ToDoViewHolder>() {


    inner class ToDoViewHolder(val binding: TaskDescriptionBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ToDoViewHolder {
        val binding = TaskDescriptionBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ToDoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ToDoViewHolder, position: Int) {
        with(holder) {
            with(list[position]) {
                val colors = itemView.resources.getIntArray(R.array.colorResources)
                val randomColor = colors[position % colors.size]

                binding.taskTitle.text = this.taskTitle
                binding.taskDesciption.text = this.taskDesc
                binding.taskDate.text = this.taskDate
                binding.taskTime.text = this.taskTime
                binding.linearLayoutTask.setBackgroundColor(randomColor)

                binding.taskCategory.text = this.category
                binding.taskPriority.text = this.priority

                val priorityColor = when (this.priority.lowercase()) {
                    "high" -> android.graphics.Color.parseColor("#55EF4444")   // Muted High-contrast Red overlay
                    "medium" -> android.graphics.Color.parseColor("#55FF9800") // Muted High-contrast Orange overlay
                    else -> android.graphics.Color.parseColor("#22FFFFFF")      // Muted grey overlay
                }
                binding.taskPriority.background?.mutate()?.setTint(priorityColor)
            }
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    fun getItem(position: Int): ToDoData {
        return list[position]
    }

}