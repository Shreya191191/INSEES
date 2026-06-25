package com.example.insees.fragment

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.insees.R
import com.example.insees.adapter.TimetableSlotAdapter
import com.example.insees.databinding.DialogAddTimetableSlotBinding
import com.example.insees.databinding.FragmentTimetableBinding
import com.example.insees.model.AttendanceSubject
import com.example.insees.model.TimetableSlot
import com.example.insees.util.AttendanceManager

class TimetableFragment : Fragment() {

    private var _binding: FragmentTimetableBinding? = null
    private val binding get() = _binding!!

    private lateinit var slotAdapter: TimetableSlotAdapter
    private var activeSem: String = "Sem_1"
    private var selectedDay: String = "Monday"
    private var subjectsList: List<AttendanceSubject> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTimetableBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupToolbar()
        setupDaySpinner()
        setupRecyclerView()
        loadActiveSemesterAndSubjects()
        setupClickListeners()
    }

    private fun setupToolbar() {
        binding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun setupDaySpinner() {
        val days = arrayOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday")
        val spinnerAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, days)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spDayOfWeek.adapter = spinnerAdapter

        binding.spDayOfWeek.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedDay = days[position]
                loadTimetableSlots()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun setupRecyclerView() {
        slotAdapter = TimetableSlotAdapter(
            onDeleteClick = { slot -> showDeleteConfirmDialog(slot) }
        )
        binding.rvTimetableSlots.layoutManager = LinearLayoutManager(context)
        binding.rvTimetableSlots.adapter = slotAdapter
    }

    private fun loadActiveSemesterAndSubjects() {
        AttendanceManager.fetchActiveSemester(
            onResult = { semNum ->
                activeSem = "Sem_$semNum"
                loadTimetableSlots()
                loadSubjects()
            },
            onFailure = { err ->
                Toast.makeText(context, "Error: $err", Toast.LENGTH_SHORT).show()
            }
        )
    }

    private fun loadSubjects() {
        AttendanceManager.fetchSubjects(activeSem,
            onResult = { subjects ->
                subjectsList = subjects
            },
            onFailure = { err ->
                Toast.makeText(context, "Failed to load subjects: $err", Toast.LENGTH_SHORT).show()
            }
        )
    }

    private fun loadTimetableSlots() {
        if (activeSem.isEmpty()) return
        AttendanceManager.fetchTimetable(activeSem, selectedDay,
            onResult = { slots ->
                slotAdapter.submitList(slots)
                val isEmpty = slots.isEmpty()
                binding.tvNoSlots.visibility = if (isEmpty) View.VISIBLE else View.GONE
                binding.rvTimetableSlots.visibility = if (isEmpty) View.GONE else View.VISIBLE
            },
            onFailure = { err ->
                Toast.makeText(context, "Failed to fetch slots: $err", Toast.LENGTH_SHORT).show()
            }
        )
    }

    private fun setupClickListeners() {
        binding.fabAddSlot.setOnClickListener {
            if (subjectsList.isEmpty()) {
                Toast.makeText(context, "Please add some subjects/labs to this semester first!", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            showAddSlotDialog()
        }
    }

    private fun showAddSlotDialog() {
        val dialogBinding = DialogAddTimetableSlotBinding.inflate(layoutInflater)
        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogBinding.root)
            .create()

        // Populate spinner with subjects
        val subjectNames = subjectsList.map { it.name }
        val subjectAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, subjectNames)
        subjectAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        dialogBinding.spDialogSubjects.adapter = subjectAdapter

        dialogBinding.btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        dialogBinding.btnSave.setOnClickListener {
            val selectedIndex = dialogBinding.spDialogSubjects.selectedItemPosition
            if (selectedIndex < 0) return@setOnClickListener

            val subject = subjectsList[selectedIndex]
            val time = dialogBinding.etDialogTime.text.toString().trim()

            if (time.isEmpty()) {
                Toast.makeText(context, "Please enter class time", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            AttendanceManager.addTimetableSlot(
                activeSem,
                selectedDay,
                subject.id,
                subject.name,
                time,
                onSuccess = {
                    Toast.makeText(context, "Class scheduled!", Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                },
                onFailure = { err ->
                    Toast.makeText(context, "Failed to add: $err", Toast.LENGTH_SHORT).show()
                }
            )
        }

        dialog.show()
    }

    private fun showDeleteConfirmDialog(slot: TimetableSlot) {
        AlertDialog.Builder(requireContext())
            .setTitle("Remove Class Slot?")
            .setMessage("Are you sure you want to remove ${slot.subjectName} from your ${slot.dayOfWeek} schedule?")
            .setPositiveButton("Remove") { _, _ ->
                AttendanceManager.deleteTimetableSlot(
                    activeSem,
                    slot.dayOfWeek,
                    slot.id,
                    onSuccess = {
                        Toast.makeText(context, "Slot removed", Toast.LENGTH_SHORT).show()
                    },
                    onFailure = { err ->
                        Toast.makeText(context, "Failed to delete: $err", Toast.LENGTH_SHORT).show()
                    }
                )
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}