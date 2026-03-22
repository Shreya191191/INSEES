package com.example.insees.Adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.insees.R
import com.example.insees.Dataclasses.ToDoData
import com.example.insees.databinding.ItemTaskBinding

class HomeToDoAdapter(private val list: MutableList<ToDoData>) :
    RecyclerView.Adapter<HomeToDoAdapter.HomeToDoViewHolder>() {
    private val colorResources = arrayOf(R.color.lighttyellow, R.color.purpleblue, R.color.mediumGreen, R.color.darkRed)
    inner class HomeToDoViewHolder(val binding: ItemTaskBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomeToDoViewHolder {
        val binding = ItemTaskBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return HomeToDoViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return list.size
    }
    fun getItem(position: Int) : ToDoData {
        return list[position]
    }

    override fun onBindViewHolder(holder: HomeToDoViewHolder, position: Int) {
        val task = list[position]
        with(holder){
            val colors = itemView.resources.getIntArray(R.array.colorResources)
            val randomColor = colors[position % colors.size]
            binding.task2.text = task.taskTitle
            binding.task2.setBackgroundColor(randomColor)
        }
    }
}