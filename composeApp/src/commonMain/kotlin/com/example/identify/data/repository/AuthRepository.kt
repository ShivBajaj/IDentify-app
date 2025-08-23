package com.example.identify.data.repository

import com.example.identify.data.model.LoginResponse
import com.example.identify.data.model.LogoutResponse
import com.example.identify.data.model.StaffLoginRequest
import com.example.identify.data.model.StudentOtpRequest
import com.example.identify.data.model.StudentOtpResponse
import com.example.identify.data.model.StudentVerifyOtpRequest
import com.example.identify.data.network.ApiRoutes
import com.example.identify.data.network.TokenStorage
import com.example.identify.presentation.auth.CurrentRole
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

class AuthRepository(private val client: HttpClient, private val tokenStorage: TokenStorage) {


    suspend fun studentSendOtp(email: String): StudentOtpResponse {
        return client.post(ApiRoutes.Student.SEND_OTP) {
            contentType(ContentType.Application.Json)
            setBody(StudentOtpRequest(email))
        }.body<StudentOtpResponse>()
    }

    suspend fun studentVerifyOtp(email: String, otp: String): LoginResponse {
        val response = client.post(ApiRoutes.Student.VERIFY_OTP) {
            contentType(ContentType.Application.Json)
            setBody(StudentVerifyOtpRequest(email, otp))
        }.body<LoginResponse>()
        if (response.token != null) {
            tokenStorage.accessToken = response.token
        }
        return response
    }


//    suspend fun studentLogout(): StudentLogoutResponse {
//        val result = client.post(ApiRoutes.Student.LOGOUT).body<StudentLogoutResponse>()
//        tokenStorage.clear()
//        return result
//    }

    suspend fun staffLogin(username: String, password: String): LoginResponse {
        val response = client.post(ApiRoutes.Staff.LOGIN) {
            contentType(ContentType.Application.Json)
            setBody(StaffLoginRequest(username, password))
        }.body<LoginResponse>()
        if (response.token != null) {
            tokenStorage.accessToken = response.token
        }
        return response
    }

    suspend fun logout(currentRole: CurrentRole): LogoutResponse {
        when(currentRole) {
            CurrentRole.STUDENT -> {
                val response = client.post(ApiRoutes.Student.LOGOUT).body<LogoutResponse>()
                tokenStorage.clear()
                return response
            }
            CurrentRole.STAFF -> {
                val response = client.post(ApiRoutes.Staff.LOGOUT).body<LogoutResponse>()
                tokenStorage.clear()
                return response
            }
            CurrentRole.LOGGED_OUT -> {
                return LogoutResponse("Not Logged in")
            }
        }

    }
}