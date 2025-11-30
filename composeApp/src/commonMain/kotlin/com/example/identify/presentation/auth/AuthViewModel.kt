package com.example.identify.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.identify.data.model.LoginResponse
import com.example.identify.data.model.LogoutResponse
import com.example.identify.data.model.StudentOtpResponse
import com.example.identify.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch


sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    object Success : AuthState()
    data class Error(val message: String) : AuthState()
}

enum class CurrentRole{
    STUDENT,
    STAFF,
    LOGGED_OUT
}

class AuthViewModel(
    private val authRepository: AuthRepository,
): ViewModel(){

    private val _username = MutableStateFlow("")
    val username: StateFlow<String> = _username

    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password

    private val _otp = MutableStateFlow("")
    val otp: StateFlow<String> = _otp

    private val _selectedRole = MutableStateFlow("Student")
    val selectedRole: StateFlow<String> = _selectedRole

    // Update functions
    fun onUsernameChange(newValue: String) { _username.value = newValue }
    fun onPasswordChange(newValue: String) { _password.value = newValue }
    fun onOtpChange(newValue: String) { _otp.value = newValue }
    fun onRoleChange(newValue: String) {
        _selectedRole.value = newValue
        _username.value = ""
        _password.value = ""
        _otp.value = ""
    }

    private val _currentRole = MutableStateFlow<CurrentRole>(CurrentRole.LOGGED_OUT)
    val currentRole: StateFlow<CurrentRole> = _currentRole

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState
    //student
    private val _otpState = MutableStateFlow<Result<StudentOtpResponse>?>(null)
    val otpState: StateFlow<Result<StudentOtpResponse>?> = _otpState

//    private val _verifyState = MutableStateFlow<Result<StudentVerifyOtpResponse>?>(null)
//    val verifyState: StateFlow<Result<StudentVerifyOtpResponse>?> = _verifyState

    private val _loginState = MutableStateFlow<Result<LoginResponse>?>(null)
    val loginState: StateFlow<Result<LoginResponse>?> = _loginState

    private val _logoutState = MutableStateFlow<Result<LogoutResponse>?>(null)
    val logoutState: StateFlow<Result<LogoutResponse>?> = _logoutState

    private val _logoutInProgress = MutableStateFlow(false)
    val logoutInProgress: StateFlow<Boolean> = _logoutInProgress

    //functions
    //student
    // Send OTP
    fun sendOtp(email: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val result = authRepository.studentSendOtp(email)
                _otpState.value = Result.success(result)
                _authState.value = AuthState.Idle
            } catch (e: Exception) {
                print("failed")
                _otpState.value = Result.failure(e)
                _authState.value = AuthState.Idle
            }
        }
    }

    // Verify OTP
    fun verifyOtp(email: String, otp: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val result = authRepository.studentVerifyOtp(email, otp)
                _loginState.value = Result.success(result)

                _authState.value = AuthState.Success
                _currentRole.value = CurrentRole.STUDENT

            } catch (e: Exception) {
                _loginState.value = Result.failure(e)
                _authState.value = AuthState.Error("OTP verification failed")
            }
        }
    }



    //staff
    // Login
    fun staffLogin(username: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val result = authRepository.staffLogin(username, password)
                _loginState.value = Result.success(result)
                _authState.value = AuthState.Success
                _currentRole.value = CurrentRole.STAFF
            } catch (e: Exception) {
                _loginState.value = Result.failure(e)
                _authState.value = AuthState.Error("Staff login failed")
            }
        }
    }

    // Logout
    fun logout() {
        viewModelScope.launch {
            _logoutInProgress.value = true
            try {
                val result = authRepository.logout(currentRole.value)
                _logoutState.value = Result.success(result)
                _authState.value = AuthState.Idle
                _currentRole.value = CurrentRole.LOGGED_OUT
                _logoutInProgress.value = false
                reset()
            } catch (e: Exception) {
                _logoutState.value = Result.failure(e)
                _authState.value = AuthState.Error("Logout failed! try again")
                _logoutInProgress.value = false
            }
        }
    }

    fun reset() {
        _currentRole.value = CurrentRole.LOGGED_OUT
        _authState.value = AuthState.Idle

        _otpState.value = null
        _loginState.value = null
        _logoutState.value = null
    }

}