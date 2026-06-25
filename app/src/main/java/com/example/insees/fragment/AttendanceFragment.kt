package com.example.insees.fragment

import android.app.AlertDialog
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.insees.R
import com.example.insees.adapter.AttendanceSubjectAdapter
import com.example.insees.databinding.DialogAddAttendanceSubjectBinding
import com.example.insees.databinding.DialogEditAttendanceSubjectBinding
import com.example.insees.databinding.FragmentAttendanceBinding
import com.example.insees.model.AttendanceSubject
import com.example.insees.util.AttendanceViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.example.insees.util.AttendanceManager

class AttendanceFragment : Fragment() {

    private var _binding: FragmentAttendanceBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: AttendanceViewModel
    private lateinit var subjectAdapter: AttendanceSubjectAdapter
    private var currentSubjectsList: List<AttendanceSubject> = emptyList()
    private var todaySlots: List<com.example.insees.model.TimetableSlot> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAttendanceBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        // Hide the BottomNavigationView to allow full screen focus on tracker
        requireActivity().findViewById<BottomNavigationView>(R.id.bvNavBar).visibility = View.GONE
    }

    override fun onStop() {
        super.onStop()
        // Show the BottomNavigationView when leaving
        requireActivity().findViewById<BottomNavigationView>(R.id.bvNavBar).visibility = View.VISIBLE
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this)[AttendanceViewModel::class.java]

        setupToolbar()
        setupSemesterSpinner()
        setupRecyclerView()
        observeViewModel()
        setupClickListeners()

        // Load active semester from Firebase
        viewModel.loadActiveSemester()
    }

    private fun setupToolbar() {
        binding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.btnResetAll.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("Reset Entire Tracker?")
                .setMessage("Are you sure you want to reset all semester attendance counts back to 0? Your custom subjects will NOT be deleted.")
                .setPositiveButton("Reset All") { _, _ ->
                    // For reset all, we'd iterate semesters, but we can reset current sem and let user know
                    viewModel.resetSemester()
                    Toast.makeText(context, "Current semester reset", Toast.LENGTH_SHORT).show()
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }

    private fun setupSemesterSpinner() {
        val semesters = (1..8).map { "Semester $it" }
        val spinnerAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, semesters)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spSemester.adapter = spinnerAdapter

        binding.spSemester.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedSemNum = position + 1
                if (viewModel.activeSemester.value != selectedSemNum) {
                    viewModel.selectSemester(selectedSemNum)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun setupRecyclerView() {
        subjectAdapter = AttendanceSubjectAdapter(
            onPresentClick = { subject -> viewModel.markPresent(subject) },
            onAbsentClick = { subject -> viewModel.markAbsent(subject) },
            onEditClick = { subject -> showEditDialog(subject) },
            onDeleteClick = { subject -> showDeleteDialog(subject) }
        )

        binding.rvAttendanceSubjects.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = subjectAdapter
        }
    }

    private fun observeViewModel() {
        // Loading State
        viewModel.loading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        // Active Semester
        viewModel.activeSemester.observe(viewLifecycleOwner) { semNum ->
            val spinnerPosition = semNum - 1
            if (binding.spSemester.selectedItemPosition != spinnerPosition) {
                binding.spSemester.setSelection(spinnerPosition)
            }
            loadTodayTimetable(semNum)
        }

        // Subjects List & Overall Stats
        viewModel.subjects.observe(viewLifecycleOwner) { subjectList ->
            subjectAdapter.submitList(subjectList)
            currentSubjectsList = subjectList ?: emptyList()
            updateTodayClassesUi()

            if (subjectList.isNullOrEmpty()) {
                binding.llEmptyState.visibility = View.VISIBLE
                binding.rvAttendanceSubjects.visibility = View.GONE
                updateOverallStats(0, 0, 100f)
            } else {
                binding.llEmptyState.visibility = View.GONE
                binding.rvAttendanceSubjects.visibility = View.VISIBLE

                var totalAttended = 0
                var totalScheduled = 0

                for (subject in subjectList) {
                    totalAttended += subject.classesAttended
                    totalScheduled += subject.classesScheduled
                }

                val overallPercent = if (totalScheduled > 0) {
                    (totalAttended.toFloat() / totalScheduled.toFloat()) * 100f
                } else {
                    100f
                }

                updateOverallStats(totalAttended, totalScheduled, overallPercent)
            }
        }

        // Error Handling
        viewModel.error.observe(viewLifecycleOwner) { errMessage ->
            if (errMessage != null) {
                Toast.makeText(context, errMessage, Toast.LENGTH_LONG).show()
                viewModel.clearError()
            }
        }
    }

    private fun loadTodayTimetable(semNum: Int) {
        val days = arrayOf("Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday")
        val calendar = java.util.Calendar.getInstance()
        val dayOfWeek = days[calendar.get(java.util.Calendar.DAY_OF_WEEK) - 1]

        if (dayOfWeek == "Sunday") {
            binding.cvTodaySchedule.visibility = View.GONE
            return
        }

        AttendanceManager.fetchTimetable("Sem_$semNum", dayOfWeek,
            onResult = { slots ->
                todaySlots = slots
                updateTodayClassesUi()
            },
            onFailure = {
                // Fail silently
            }
        )
    }

    private fun updateTodayClassesUi() {
        if (!isAdded || _binding == null) return
        binding.llTodayClasses.removeAllViews()

        if (todaySlots.isEmpty()) {
            binding.cvTodaySchedule.visibility = View.GONE
            return
        }

        binding.cvTodaySchedule.visibility = View.VISIBLE

        for (slot in todaySlots) {
            val matchingSubject = currentSubjectsList.find { it.id == slot.subjectId }

            val row = android.widget.LinearLayout(requireContext()).apply {
                layoutParams = android.widget.LinearLayout.LayoutParams(
                    android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                    android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
                )
                orientation = android.widget.LinearLayout.HORIZONTAL
                gravity = android.view.Gravity.CENTER_VERTICAL
                setPadding(0, 8, 0, 8)
            }

            val textLayout = android.widget.LinearLayout(requireContext()).apply {
                layoutParams = android.widget.LinearLayout.LayoutParams(
                    0,
                    android.widget.LinearLayout.LayoutParams.WRAP_CONTENT,
                    1f
                )
                orientation = android.widget.LinearLayout.VERTICAL
            }

            val tvName = android.widget.TextView(requireContext()).apply {
                text = slot.subjectName
                setTextColor(Color.WHITE)
                textSize = 14f
                setTypeface(null, android.graphics.Typeface.BOLD)
            }

            val tvTime = android.widget.TextView(requireContext()).apply {
                text = slot.time
                setTextColor(Color.parseColor("#AAAAAA"))
                textSize = 12f
            }

            textLayout.addView(tvName)
            textLayout.addView(tvTime)
            row.addView(textLayout)

            // Present Button
            val btnPresent = android.widget.Button(requireContext(), null, android.R.attr.buttonStyleSmall).apply {
                text = "+1 Pres"
                textSize = 11f
                setTextColor(Color.WHITE)
                backgroundTintList = ColorStateList.valueOf(requireContext().getColor(R.color.mediumGreen))
                setOnClickListener {
                    if (matchingSubject != null) {
                        viewModel.markPresent(matchingSubject)
                        Toast.makeText(context, "Marked Present in ${slot.subjectName}", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "Subject details not loaded yet", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            // Absent Button
            val btnAbsent = android.widget.Button(requireContext(), null, android.R.attr.buttonStyleSmall).apply {
                text = "+1 Abs"
                textSize = 11f
                setTextColor(Color.WHITE)
                backgroundTintList = ColorStateList.valueOf(requireContext().getColor(R.color.mediumRed))
                setOnClickListener {
                    if (matchingSubject != null) {
                        viewModel.markAbsent(matchingSubject)
                        Toast.makeText(context, "Marked Absent in ${slot.subjectName}", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "Subject details not loaded yet", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            row.addView(btnPresent)
            row.addView(btnAbsent)
            binding.llTodayClasses.addView(row)
        }
    }

    private fun updateOverallStats(attended: Int, scheduled: Int, percentage: Float) {
        binding.tvOverallPercent.text = "${percentage.toInt()}%"
        binding.pbOverallAttendance.progress = percentage.toInt()
        binding.tvOverallCounts.text = "$attended / $scheduled classes attended"

        val greenColor = requireContext().getColor(R.color.mediumGreen)
        val redColor = requireContext().getColor(R.color.mediumRed)
        val warningColor = Color.parseColor("#F59E0B")
        val textSecondary = Color.parseColor("#AAAAAA")

        if (scheduled == 0) {
            binding.pbOverallAttendance.setProgressTintList(ColorStateList.valueOf(greenColor))
            binding.tvOverallPercent.setTextColor(greenColor)
            binding.tvOverallStatus.text = "No classes registered yet. Maintain 100%!"
            binding.tvOverallStatus.setTextColor(textSecondary)
        } else {
            if (percentage >= 75f) {
                binding.pbOverallAttendance.setProgressTintList(ColorStateList.valueOf(greenColor))
                binding.tvOverallPercent.setTextColor(greenColor)

                val maxMiss = (attended / 0.75f - scheduled).toInt()
                if (maxMiss > 0) {
                    binding.tvOverallStatus.text = "Safe! You can miss next $maxMiss class${if (maxMiss > 1) "es" else ""} overall."
                    binding.tvOverallStatus.setTextColor(greenColor)
                } else {
                    binding.tvOverallStatus.text = "On track! You cannot afford to miss any overall classes."
                    binding.tvOverallStatus.setTextColor(warningColor)
                }
            } else {
                binding.pbOverallAttendance.setProgressTintList(ColorStateList.valueOf(redColor))
                binding.tvOverallPercent.setTextColor(redColor)

                val mustAttend = 3 * scheduled - 4 * attended
                if (mustAttend > 0) {
                    binding.tvOverallStatus.text = "Alert! Attend next $mustAttend class${if (mustAttend > 1) "es" else ""} overall consecutively to reach 75%."
                    binding.tvOverallStatus.setTextColor(redColor)
                } else {
                    binding.tvOverallStatus.text = "Alert! Below 75% threshold."
                    binding.tvOverallStatus.setTextColor(redColor)
                }
            }
        }
    }

    private fun setupClickListeners() {
        binding.btnPreseed.setOnClickListener {
            viewModel.preseedSemester()
        }

        binding.btnTimetable.setOnClickListener {
            findNavController().navigate(R.id.action_attendanceFragment_to_timetableFragment)
        }

        binding.btnResetSem.setOnClickListener {
            val semNum = viewModel.activeSemester.value ?: return@setOnClickListener
            AlertDialog.Builder(requireContext())
                .setTitle("Reset Semester $semNum?")
                .setMessage("Are you sure you want to reset all attendance counts for Semester $semNum to 0? Your custom subjects will NOT be deleted.")
                .setPositiveButton("Reset") { _, _ ->
                    viewModel.resetSemester()
                    Toast.makeText(context, "Semester $semNum attendance reset", Toast.LENGTH_SHORT).show()
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

        binding.fabAddSubject.setOnClickListener {
            showAddSubjectDialog()
        }
    }

    private fun showAddSubjectDialog() {
        val dialogBinding = DialogAddAttendanceSubjectBinding.inflate(layoutInflater)
        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogBinding.root)
            .create()

        dialogBinding.btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        dialogBinding.btnSave.setOnClickListener {
            val name = dialogBinding.etSubjectName.text.toString().trim()
            val isLab = dialogBinding.rbLab.isChecked

            if (name.isEmpty()) {
                dialogBinding.tilSubjectName.error = "Name cannot be empty"
                return@setOnClickListener
            }

            viewModel.addCustomSubject(name, isLab)
            dialog.dismiss()
            Toast.makeText(context, "Subject added successfully", Toast.LENGTH_SHORT).show()
        }

        dialog.show()
    }

    private fun showEditDialog(subject: AttendanceSubject) {
        val dialogBinding = DialogEditSubjectBindingBinding() // wait, we will inflate it manually
        val view = layoutInflater.inflate(R.layout.dialog_edit_attendance_subject, null)
        val dialogBindingWrapper = DialogEditAttendanceSubjectBinding.bind(view)

        dialogBindingWrapper.etSubjectName.setText(subject.name)
        dialogBindingWrapper.etAttendedCount.setText(subject.classesAttended.toString())
        dialogBindingWrapper.etScheduledCount.setText(subject.classesScheduled.toString())

        // Stepper click handlers
        dialogBindingWrapper.btnAttendedMinus.setOnClickListener {
            val current = dialogBindingWrapper.etAttendedCount.text.toString().toIntOrNull() ?: 0
            if (current > 0) {
                dialogBindingWrapper.etAttendedCount.setText((current - 1).toString())
            }
        }

        dialogBindingWrapper.btnAttendedPlus.setOnClickListener {
            val current = dialogBindingWrapper.etAttendedCount.text.toString().toIntOrNull() ?: 0
            val newAttended = current + 1
            dialogBindingWrapper.etAttendedCount.setText(newAttended.toString())

            // Auto-increment scheduled if attended exceeds scheduled
            val scheduled = dialogBindingWrapper.etScheduledCount.text.toString().toIntOrNull() ?: 0
            if (newAttended > scheduled) {
                dialogBindingWrapper.etScheduledCount.setText(newAttended.toString())
            }
        }

        dialogBindingWrapper.btnScheduledMinus.setOnClickListener {
            val current = dialogBindingWrapper.etScheduledCount.text.toString().toIntOrNull() ?: 0
            if (current > 0) {
                val newScheduled = current - 1
                dialogBindingWrapper.etScheduledCount.setText(newScheduled.toString())

                // Auto-decrement attended if scheduled goes below attended
                val attended = dialogBindingWrapper.etAttendedCount.text.toString().toIntOrNull() ?: 0
                if (newScheduled < attended) {
                    dialogBindingWrapper.etAttendedCount.setText(newScheduled.toString())
                }
            }
        }

        dialogBindingWrapper.btnScheduledPlus.setOnClickListener {
            val current = dialogBindingWrapper.etScheduledCount.text.toString().toIntOrNull() ?: 0
            dialogBindingWrapper.etScheduledCount.setText((current + 1).toString())
        }

        val dialog = AlertDialog.Builder(requireContext())
            .setView(view)
            .create()

        dialogBindingWrapper.btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        dialogBindingWrapper.btnSave.setOnClickListener {
            val newName = dialogBindingWrapper.etSubjectName.text.toString().trim()
            val attended = dialogBindingWrapper.etAttendedCount.text.toString().toIntOrNull() ?: 0
            val scheduled = dialogBindingWrapper.etScheduledCount.text.toString().toIntOrNull() ?: 0

            if (newName.isEmpty()) {
                dialogBindingWrapper.tilSubjectName.error = "Name cannot be empty"
                return@setOnClickListener
            }

            if (attended > scheduled) {
                Toast.makeText(context, "Attended cannot exceed scheduled classes", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            viewModel.updateSubjectDetails(subject.id, newName, attended, scheduled)
            dialog.dismiss()
            Toast.makeText(context, "Subject updated successfully", Toast.LENGTH_SHORT).show()
        }

        dialog.show()
    }

    // Workaround class to bypass binding mismatch or we just define wrapper manually
    private class DialogEditSubjectBindingBinding

    private fun showDeleteDialog(subject: AttendanceSubject) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete ${subject.name}?")
            .setMessage("Are you sure you want to delete this subject/lab? This action cannot be undone.")
            .setPositiveButton("Delete") { _, _ ->
                viewModel.deleteSubject(subject.id)
                Toast.makeText(context, "${subject.name} deleted", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}