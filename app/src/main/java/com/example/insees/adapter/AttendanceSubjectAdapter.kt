package com.example.insees.adapter

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.insees.R
import com.example.insees.databinding.ItemAttendanceSubjectBinding
import com.example.insees.model.AttendanceSubject

class AttendanceSubjectAdapter(
    private val onPresentClick: (AttendanceSubject) -> Unit,
    private val onAbsentClick: (AttendanceSubject) -> Unit,
    private val onEditClick: (AttendanceSubject) -> Unit,
    private val onDeleteClick: (AttendanceSubject) -> Unit
) : ListAdapter<AttendanceSubject, AttendanceSubjectAdapter.ViewHolder>(SubjectDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemAttendanceSubjectBinding.inflate(
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
        private val binding: ItemAttendanceSubjectBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(subject: AttendanceSubject) {
            val context = binding.root.context

            // Name
            binding.tvSubjectName.text = subject.name

            // Badge
            if (subject.isLab) {
                binding.tvTypeBadge.text = "LAB"
                binding.tvTypeBadge.setBackgroundResource(R.drawable.rounded_textview_bg)
                binding.tvTypeBadge.setBackgroundColor(Color.parseColor("#4B5563")) // Gray-600
            } else {
                binding.tvTypeBadge.text = "SUBJECT"
                binding.tvTypeBadge.setBackgroundResource(R.drawable.rounded_textview_bg)
                binding.tvTypeBadge.setBackgroundColor(Color.parseColor("#6F4BD7")) // Purple Theme
            }

            val attended = subject.classesAttended
            val scheduled = subject.classesScheduled
            val percent = subject.attendancePercentage

            binding.tvPercentage.text = "${percent.toInt()}%"
            binding.tvAttendanceCounts.text = "$attended / $scheduled classes"
            binding.pbAttendance.progress = percent.toInt()

            val greenColor = context.getColor(R.color.mediumGreen)
            val redColor = context.getColor(R.color.mediumRed)
            val warningColor = Color.parseColor("#F59E0B") // Amber/Yellow
            val textSecondary = Color.parseColor("#AAAAAA")

            if (scheduled == 0) {
                binding.tvPercentage.setTextColor(greenColor)
                binding.pbAttendance.setProgressTintList(ColorStateList.valueOf(greenColor))
                binding.tvStatusMessage.text = "No classes scheduled yet. Maintain 100%!"
                binding.tvStatusMessage.setTextColor(textSecondary)
            } else {
                if (percent >= 75f) {
                    binding.tvPercentage.setTextColor(greenColor)
                    binding.pbAttendance.setProgressTintList(ColorStateList.valueOf(greenColor))

                    // Safe to miss calculations
                    val maxMiss = (attended / 0.75f - scheduled).toInt()
                    if (maxMiss > 0) {
                        binding.tvStatusMessage.text = "Safe! You can miss next $maxMiss class${if (maxMiss > 1) "es" else ""}."
                        binding.tvStatusMessage.setTextColor(greenColor)
                    } else {
                        binding.tvStatusMessage.text = "On track! You cannot afford to miss the next class."
                        binding.tvStatusMessage.setTextColor(warningColor)
                    }
                } else {
                    binding.tvPercentage.setTextColor(redColor)
                    binding.pbAttendance.setProgressTintList(ColorStateList.valueOf(redColor))

                    // Must attend calculations
                    val mustAttend = 3 * scheduled - 4 * attended
                    if (mustAttend > 0) {
                        binding.tvStatusMessage.text = "Alert! Attend next $mustAttend class${if (mustAttend > 1) "es" else ""} consecutively to reach 75%."
                        binding.tvStatusMessage.setTextColor(redColor)
                    } else {
                        binding.tvStatusMessage.text = "Alert! Below 75% threshold."
                        binding.tvStatusMessage.setTextColor(redColor)
                    }
                }
            }

            // Click listeners
            binding.btnPresent.setOnClickListener { onPresentClick(subject) }
            binding.btnAbsent.setOnClickListener { onAbsentClick(subject) }
            binding.btnEdit.setOnClickListener { onEditClick(subject) }
            binding.btnDelete.setOnClickListener { onDeleteClick(subject) }
        }
    }

    class SubjectDiffCallback : DiffUtil.ItemCallback<AttendanceSubject>() {
        override fun areItemsTheSame(oldItem: AttendanceSubject, newItem: AttendanceSubject): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: AttendanceSubject, newItem: AttendanceSubject): Boolean {
            return oldItem == newItem
        }
    }
}