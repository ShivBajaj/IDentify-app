package com.example.identify.presentation.VerifyQR

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.identify.data.model.VerifyQrResponse
import com.example.identify.data.repository.VerificationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

enum class VerifyQrStatus{
    IDLE,
    LOADING,
    SUCCESS
}
class VerifyQrViewModel(
    private val verifyQrRepository: VerificationRepository,
): ViewModel() {

    private var _verifyQrState = MutableStateFlow<Result<VerifyQrResponse>?>(null)
    val verifyQrState: StateFlow<Result<VerifyQrResponse>?> = _verifyQrState

    private var _status = MutableStateFlow<VerifyQrStatus>(VerifyQrStatus.IDLE)
    val status: StateFlow<VerifyQrStatus> = _status

    fun verifyQr(qrData: String){
        viewModelScope.launch {
            _status.value = VerifyQrStatus.LOADING
            try{
                val result = verifyQrRepository.verifyQr(qrData)
                _verifyQrState.value = Result.success(result)
                _status.value = VerifyQrStatus.SUCCESS
            }catch (e: Exception){
                _status.value = VerifyQrStatus.SUCCESS
                _verifyQrState.value = Result.failure(e)
            }
        }
    }

    init {
        reset()
    }
    fun reset(){
        _verifyQrState.value = null
        _status.value = VerifyQrStatus.IDLE
    }
}