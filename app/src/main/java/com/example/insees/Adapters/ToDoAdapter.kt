package com.example.insees.Adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.insees.R
import com.example.insees.Dataclasses.ToDoData
import com.example.insees.databinding.TaskDescriptionBinding

class ToDoAdapter(private val list: MutableList<ToDoData>)
    : RecyclerView.Adapter<ToDoAdapter.ToDoViewHolder>() {

//    private var listener: ToDoAdapterClicksInterface? = null
//
//    fun setListener(listener: ToDoAdapterClicksInterface) {
//        this.listener = listener
//    }

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
//                binding.btnDeleteTask.setOnClickListener {
//                    listener?.onDeleteTaskBtnClicked(this) // Pass the entire ToDoData object
//                }
            }
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    fun getItem(position: Int): ToDoData {
        return list[position]
    }

//    interface ToDoAdapterClicksInterface {
//        fun onDeleteTaskBtnClicked(toDoData: ToDoData)
//    }
}