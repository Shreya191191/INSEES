package com.example.insees.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.insees.databinding.ItemTimetableSlotBinding
import com.example.insees.model.TimetableSlot

class TimetableSlotAdapter(
    private val onDeleteClick: (TimetableSlot) -> Unit
) : ListAdapter<TimetableSlot, TimetableSlotAdapter.ViewHolder>(TimetableDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemTimetableSlotBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(
        private val binding: ItemTimetableSlotBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(slot: TimetableSlot) {
            binding.tvSlotSubjectName.text = slot.subjectName
            binding.tvSlotTime.text = slot.time

            binding.btnDeleteSlot.setOnClickListener {
                onDeleteClick(slot)
            }
        }
    }
}

class TimetableDiffCallback : DiffUtil.ItemCallback<TimetableSlot>() {
    override fun areItemsTheSame(oldItem: TimetableSlot, newItem: TimetableSlot): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: TimetableSlot, newItem: TimetableSlot): Boolean {
        return oldItem == newItem
    }
}