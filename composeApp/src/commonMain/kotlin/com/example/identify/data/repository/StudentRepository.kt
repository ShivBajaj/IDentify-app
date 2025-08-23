package com.example.identify.data.repository

import com.example.identify.data.model.StudentDetailsResponse
import com.example.identify.data.network.ApiRoutes
import com.example.identify.data.network.TokenStorage
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.headers

class StudentRepository(private val client: HttpClient, private val tokenStorage: TokenStorage) {

    suspend fun fetchDetails(): StudentDetailsResponse {
        if (!tokenStorage.hasValidToken()) {
            throw IllegalStateException("No valid authentication token")
        }
        return client.get(ApiRoutes.Student.DETAILS).body<StudentDetailsResponse>()
    }

    suspend fun downloadIdCard(): ByteArray {
        return client.get(ApiRoutes.Student.DOWNLOAD) {
            headers { append("Accept", "application/pdf") }
        }.body<ByteArray>()
    }
}
