package com.example.identify.data.network

import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.http.encodedPath
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

fun createHttpClient(
    engine: HttpClientEngine,
    tokenStorage: TokenStorage = InMemoryTokenStorage()
): HttpClient {
    
    return HttpClient(engine) {
        
        install(ContentNegotiation) {
            json(
                Json {
                    ignoreUnknownKeys = true
                }
            )
        }
        
        install(Logging) {
            level = LogLevel.ALL
        }
        
        install(Auth) {
            bearer {

                loadTokens {
                    // Only return tokens if access token is not empty
                    if (tokenStorage.accessToken.isNotEmpty()) {
                        BearerTokens(
                            accessToken = tokenStorage.accessToken,
                            refreshToken = tokenStorage.refreshToken.ifEmpty { "" }
                        )
                    } else {
                        null // Return null when no valid token
                    }
                }

                sendWithoutRequest { request ->

                    val path = request.url.encodedPath

                    // Define endpoints that don't need authentication
                    val noAuthEndpoints = listOf(
                        ApiRoutes.Staff.LOGIN,
                        ApiRoutes.Student.SEND_OTP,
                        ApiRoutes.Student.VERIFY_OTP,
                        ApiRoutes.VerifyQr.VERIFY_QR,
                    )

                    // Check if current path needs auth
                    val isAuthEndpoint = noAuthEndpoints.any { endpoint ->
                        path.contains(endpoint.substringAfter(ApiRoutes.BASE_URL))
                    }

                    // Send token if: NOT an auth endpoint AND token is available
                    !isAuthEndpoint && tokenStorage.accessToken.isNotEmpty()
                }
            }
        }
    }
}

