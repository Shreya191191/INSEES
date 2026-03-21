package com.example.insees.Utils

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.insees.databinding.TaskDescriptionBinding

class ToDoAdapter(private val list: MutableList<ToDoData>)
    : RecyclerView.Adapter<ToDoAdapter.ToDoViewHolder>() {

    private var listener: ToDoAdapterClicksInterface? = null

    fun setListener(listener: ToDoAdapterClicksInterface) {
        this.listener = listener
    }

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
                binding.taskTitle.text = this.taskTitle
                binding.taskDesciption.text = this.taskDesc
                binding.taskDate.text = this.taskDate
                binding.taskTime.text = this.taskTime

                binding.btnDeleteTask.setOnClickListener {
                    listener?.onDeleteTaskBtnClicked(this) // Pass the entire ToDoData object
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    interface ToDoAdapterClicksInterface {
        fun onDeleteTaskBtnClicked(toDoData: ToDoData)
    }
}