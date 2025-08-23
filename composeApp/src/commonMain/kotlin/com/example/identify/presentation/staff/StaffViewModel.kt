package com.example.identify.presentation.staff

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.identify.data.model.FaceScanResponse
import com.example.identify.data.repository.StaffRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class StaffViewModel(
    private val staffRepository: StaffRepository,
): ViewModel() {
    private val _scanResult = MutableStateFlow<Result<FaceScanResponse>?>(null)
    val scanResult: StateFlow<Result<FaceScanResponse>?> = _scanResult

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun scanFace(imageBytes: ByteArray) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val result = staffRepository.scanFace(imageBytes)
                _scanResult.value = Result.success(result)
            } catch (e: Exception) {
                _scanResult.value = Result.failure(e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearResult() {
        _scanResult.value = null
    }

}
