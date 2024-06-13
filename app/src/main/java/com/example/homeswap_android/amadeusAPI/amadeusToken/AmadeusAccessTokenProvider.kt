package com.example.homeswap_android.amadeusAPI.amadeusToken

import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject

class AmadeusAccessTokenProvider(
    private val clientId: String,
    private val clientSecret: String
) : AccessTokenProvider {

    @Volatile private var accessToken: String? = null
    @Volatile private var expiryTime: Long = 0

    override fun token(): String? = accessToken

    override fun isTokenNullOrExpired(): Boolean {
        val currentTime = System.currentTimeMillis()
        return accessToken == null || currentTime >= expiryTime
    }

    override fun refreshToken(): String? {
        val client = OkHttpClient()
        val request = Request.Builder()
            .url("https://test.api.amadeus.com/v1/security/oauth2/token")
            .post(
                okhttp3.FormBody.Builder()
                    .add("grant_type", "client_credentials")
                    .add("client_id", clientId)
                    .add("client_secret", clientSecret)
                    .build()
            )
            .addHeader("Content-Type", "application/x-www-form-urlencoded")
            .build()

        val response = client.newCall(request).execute()
        if (response.isSuccessful) {
            response.body?.let { responseBody ->
                val json = JSONObject(responseBody.string())
                accessToken = json.getString("access_token")
                val expiresIn = json.getLong("expires_in")
                expiryTime = System.currentTimeMillis() + (expiresIn * 1000)
                return accessToken
            }
        }
        return null
    }
}
