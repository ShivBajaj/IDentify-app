package com.example.identify.data.di

import com.example.identify.data.network.InMemoryTokenStorage
import com.example.identify.data.network.TokenStorage
import com.example.identify.data.network.createHttpClient
import com.example.identify.data.repository.AuthRepository
import com.example.identify.data.repository.StaffRepository
import com.example.identify.data.repository.StudentRepository
import com.example.identify.data.repository.VerificationRepository
import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine


expect fun createHttpClientEngine(): HttpClientEngine


object AppContainer {
    private val engine: HttpClientEngine = createHttpClientEngine()
    private val tokenStorage: TokenStorage = InMemoryTokenStorage()

    private val client: HttpClient = createHttpClient(engine, tokenStorage)

    val authRepository = AuthRepository(client, tokenStorage)
    val staffRepository = StaffRepository(client, tokenStorage)
    val studentRepository = StudentRepository(client, tokenStorage)
    val verificationRepository = VerificationRepository(client)
}