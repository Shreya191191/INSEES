package com.example.insees.util

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.insees.model.AttendanceSubject

class AttendanceViewModel : ViewModel() {

    private val _activeSemester = MutableLiveData<Int>(1)
    val activeSemester: LiveData<Int> get() = _activeSemester

    private val _subjects = MutableLiveData<List<AttendanceSubject>>()
    val subjects: LiveData<List<AttendanceSubject>> get() = _subjects

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> get() = _loading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> get() = _error

    fun loadActiveSemester() {
        _loading.value = true
        AttendanceManager.fetchActiveSemester(
            onResult = { semNum ->
                _activeSemester.value = semNum
                _loading.value = false
                loadSubjects(semNum)
            },
            onFailure = { err ->
                _error.value = err
                _loading.value = false
            }
        )
    }

    fun selectSemester(semNum: Int) {
        _activeSemester.value = semNum
        _loading.value = true
        AttendanceManager.saveActiveSemester(semNum,
            onSuccess = {
                _loading.value = false
                loadSubjects(semNum)
            },
            onFailure = { err ->
                _error.value = err
                _loading.value = false
                // Load subjects anyway for local UI consistency
                loadSubjects(semNum)
            }
        )
    }

    private fun loadSubjects(semNum: Int) {
        val semString = "Sem_$semNum"
        AttendanceManager.fetchSubjects(semString,
            onResult = { subjectList ->
                _subjects.value = subjectList
                _error.value = null
            },
            onFailure = { err ->
                _error.value = err
            }
        )
    }

    fun markPresent(subject: AttendanceSubject) {
        val semNum = _activeSemester.value ?: return
        val updatedSubject = subject.copy(
            classesAttended = subject.classesAttended + 1,
            classesScheduled = subject.classesScheduled + 1
        )
        AttendanceManager.updateSubject("Sem_$semNum", updatedSubject, {}, { _error.value = it })
    }

    fun markAbsent(subject: AttendanceSubject) {
        val semNum = _activeSemester.value ?: return
        val updatedSubject = subject.copy(
            classesScheduled = subject.classesScheduled + 1
        )
        AttendanceManager.updateSubject("Sem_$semNum", updatedSubject, {}, { _error.value = it })
    }

    fun addCustomSubject(name: String, isLab: Boolean) {
        val semNum = _activeSemester.value ?: return
        AttendanceManager.addSubject("Sem_$semNum", name, isLab, {}, { _error.value = it })
    }

    fun updateSubjectDetails(subjectId: String, name: String, attended: Int, scheduled: Int) {
        val semNum = _activeSemester.value ?: return
        val currentSubject = _subjects.value?.find { it.id == subjectId } ?: return
        val updatedSubject = currentSubject.copy(
            name = name,
            classesAttended = attended,
            classesScheduled = scheduled
        )
        AttendanceManager.updateSubject("Sem_$semNum", updatedSubject, {}, { _error.value = it })
    }

    fun deleteSubject(subjectId: String) {
        val semNum = _activeSemester.value ?: return
        AttendanceManager.deleteSubject("Sem_$semNum", subjectId, {}, { _error.value = it })
    }

    fun preseedSemester() {
        val semNum = _activeSemester.value ?: return
        val isUpperSem = semNum >= 7
        AttendanceManager.preseedSemester("Sem_$semNum", isUpperSem, {}, { _error.value = it })
    }

    fun resetSemester() {
        val semNum = _activeSemester.value ?: return
        val subjectList = _subjects.value ?: return
        AttendanceManager.resetSemester("Sem_$semNum", subjectList, {}, { _error.value = it })
    }

    fun clearError() {
        _error.value = null
    }
}