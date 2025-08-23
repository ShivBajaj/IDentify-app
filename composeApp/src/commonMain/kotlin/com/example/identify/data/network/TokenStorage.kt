package com.example.identify.data.network

// Shared TokenStorage interface
interface TokenStorage {
    var accessToken: String
    var refreshToken: String // If you add refresh token support later
    fun clear()
    fun hasValidToken(): Boolean
}

// Example (replace with actual implementations per platform)
class InMemoryTokenStorage : TokenStorage {
    override var accessToken: String = ""
    override var refreshToken: String = ""
    override fun clear() {
        accessToken = ""
        refreshToken = ""
    }
    override fun hasValidToken(): Boolean {
        return accessToken.isNotEmpty()
    }
}
