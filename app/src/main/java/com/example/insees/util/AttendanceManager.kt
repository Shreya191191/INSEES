package com.example.insees.util

import com.example.insees.model.AttendanceSubject
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener

object AttendanceManager {
    private val auth = FirebaseManager.getFirebaseAuth()

    private fun userRef(): DatabaseReference {
        val uid = auth.currentUser!!.uid
        return FirebaseManager.getFirebaseDatabase().reference.child("users").child(uid)
    }

    private fun attendanceRef(semester: String): DatabaseReference {
        return userRef().child("Attendance").child(semester)
    }

    fun fetchActiveSemester(onResult: (Int) -> Unit, onFailure: (String) -> Unit) {
        userRef().child("activeSemester").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val semNum = snapshot.getValue(Int::class.java) ?: 1
                onResult(semNum)
            }
            override fun onCancelled(error: DatabaseError) {
                onFailure(error.message)
            }
        })
    }

    fun saveActiveSemester(semNum: Int, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        userRef().child("activeSemester").setValue(semNum)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it.message ?: "Failed to save active semester") }
    }

    fun fetchSubjects(semester: String, onResult: (List<AttendanceSubject>) -> Unit, onFailure: (String) -> Unit) {
        attendanceRef(semester).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<AttendanceSubject>()
                for (child in snapshot.children) {
                    val subject = child.getValue(AttendanceSubject::class.java)
                    if (subject != null) {
                        list.add(subject)
                    }
                }
                // Sort: Subjects first, then Labs, then alphabetically
                list.sortWith(compareBy({ it.isLab }, { it.name }))
                onResult(list)
            }

            override fun onCancelled(error: DatabaseError) {
                onFailure(error.message)
            }
        })
    }

    fun addSubject(semester: String, name: String, isLab: Boolean, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        val ref = attendanceRef(semester).push()
        val subjectId = ref.key ?: return
        val subject = AttendanceSubject(subjectId, name, isLab, 0, 0)
        ref.setValue(subject)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it.message ?: "Failed to add subject") }
    }

    fun updateSubject(semester: String, subject: AttendanceSubject, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        attendanceRef(semester).child(subject.id).setValue(subject)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it.message ?: "Failed to update subject") }
    }

    fun deleteSubject(semester: String, subjectId: String, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        attendanceRef(semester).child(subjectId).removeValue()
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it.message ?: "Failed to delete subject") }
    }

    fun preseedSemester(semester: String, isUpperSem: Boolean, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        val ref = attendanceRef(semester)
        val defaultSubjects = if (isUpperSem) {
            listOf("Subject 1", "Subject 2", "Subject 3")
        } else {
            listOf("Subject 1", "Subject 2", "Subject 3", "Subject 4", "Subject 5")
        }
        val defaultLabs = if (isUpperSem) emptyList() else listOf("Lab 1", "Lab 2", "Lab 3", "Lab 4")

        val batch = mutableMapOf<String, Any>()
        for (name in defaultSubjects) {
            val key = ref.push().key ?: continue
            batch[key] = AttendanceSubject(key, name, false, 0, 0)
        }
        for (name in defaultLabs) {
            val key = ref.push().key ?: continue
            batch[key] = AttendanceSubject(key, name, true, 0, 0)
        }

        ref.updateChildren(batch)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it.message ?: "Preseeding failed") }
    }

    fun resetSemester(semester: String, subjects: List<AttendanceSubject>, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        val ref = attendanceRef(semester)
        val batch = mutableMapOf<String, Any>()
        for (sub in subjects) {
            batch["${sub.id}/classesAttended"] = 0
            batch["${sub.id}/classesScheduled"] = 0
        }
        ref.updateChildren(batch)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it.message ?: "Reset failed") }
    }

    private fun timetableRef(semester: String, dayOfWeek: String): DatabaseReference {
        return userRef().child("Timetable").child(semester).child(dayOfWeek)
    }

    fun fetchTimetable(
        semester: String,
        dayOfWeek: String,
        onResult: (List<com.example.insees.model.TimetableSlot>) -> Unit,
        onFailure: (String) -> Unit
    ) {
        timetableRef(semester, dayOfWeek).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<com.example.insees.model.TimetableSlot>()
                for (child in snapshot.children) {
                    val slot = child.getValue(com.example.insees.model.TimetableSlot::class.java)
                    if (slot != null) {
                        list.add(slot)
                    }
                }
                list.sortBy { it.time }
                onResult(list)
            }

            override fun onCancelled(error: DatabaseError) {
                onFailure(error.message)
            }
        })
    }

    fun addTimetableSlot(
        semester: String,
        dayOfWeek: String,
        subjectId: String,
        subjectName: String,
        time: String,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        val ref = timetableRef(semester, dayOfWeek).push()
        val slotId = ref.key ?: return
        val slot = com.example.insees.model.TimetableSlot(slotId, subjectId, subjectName, dayOfWeek, time)
        ref.setValue(slot)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it.message ?: "Failed to add timetable slot") }
    }

    fun deleteTimetableSlot(
        semester: String,
        dayOfWeek: String,
        slotId: String,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        timetableRef(semester, dayOfWeek).child(slotId).removeValue()
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it.message ?: "Failed to delete slot") }
    }
}