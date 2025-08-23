package com.example.identify.data.repository

import com.example.identify.data.model.FaceScanResponse
import com.example.identify.data.network.ApiRoutes
import com.example.identify.data.network.TokenStorage
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.Headers

class StaffRepository(private val client: HttpClient, private val tokenStorage: TokenStorage) {

    suspend fun scanFace(image: ByteArray): FaceScanResponse {
        val response = client.post(ApiRoutes.Staff.SCAN) {
            setBody(
                MultiPartFormDataContent(
                    formData {
                        append(
                            key = "file",
                            value = image,
                            headers = Headers.build {
                                append("Content-Type", "image/jpeg")
                                append("Content-Disposition", "form-data; name=\"file\"; filename=\"photo.jpg\"")
                            }
                        )
                    }
                )
            )
        }.body<FaceScanResponse>()

        return response
    }
}

