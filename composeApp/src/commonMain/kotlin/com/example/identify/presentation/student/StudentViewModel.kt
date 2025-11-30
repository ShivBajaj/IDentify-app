package com.example.identify.presentation.student

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.identify.data.model.StudentDetailsResponse
import com.example.identify.data.repository.StudentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch



class StudentViewModel(
    private val studentRepository: StudentRepository,
): ViewModel() {
    // State

    private val _studentDetailsState = MutableStateFlow<Result<StudentDetailsResponse>?>(null)
    val studentDetailsState: StateFlow<Result<StudentDetailsResponse>?> = _studentDetailsState

    private val _idCardState = MutableStateFlow<Result<ByteArray>?>(null)
    val idCardState: StateFlow<Result<ByteArray>?> = _idCardState

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    // Get Student Details
    fun fetchDetails() {
        viewModelScope.launch {
            try {
                val result = studentRepository.fetchDetails()
                _studentDetailsState.value = Result.success(result)
            } catch (e: Exception) {
                _studentDetailsState.value = Result.failure(e)
            }
        }
    }

    fun downloadIdCard() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val result = studentRepository.downloadIdCard()
                val path = saveFileToDisk(result, "idcard.pdf") // Save it
                println("Saved PDF at: $path")
                _idCardState.value = Result.success(result)
                _isLoading.value = false
            } catch (e: Exception) {
                _idCardState.value = Result.failure(e)
                _isLoading.value = false
            }
        }
    }

    init {
        fetchDetails()
    }

}
