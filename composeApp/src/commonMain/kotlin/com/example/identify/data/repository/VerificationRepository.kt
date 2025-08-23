package com.example.identify.data.repository

import com.example.identify.data.model.VerifyQrRequest
import com.example.identify.data.model.VerifyQrResponse
import com.example.identify.data.network.ApiRoutes
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.headers
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

class VerificationRepository(private val client: HttpClient) {

    suspend fun verifyQr(qrData:String): VerifyQrResponse{

        val response = client.post(ApiRoutes.VerifyQr.VERIFY_QR){
            contentType(ContentType.Application.Json)
            setBody(VerifyQrRequest(qrData))
        }.body<VerifyQrResponse>()

        return response
    }

}