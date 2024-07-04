package com.example.homeswap_android.amadeusAPI.amadeusToken

import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route

class AccessTokenAuthenticator(
    private val tokenProvider: AccessTokenProvider
) : Authenticator {

    override fun authenticate(route: Route?, response: Response): Request? {
        synchronized(this) {
            if (tokenProvider.isTokenNullOrExpired()) {
                val token = tokenProvider.refreshToken() ?: return null
                return response.request
                    .newBuilder()
                    .removeHeader("Authorization")
                    .addHeader("Authorization", "Bearer $token")
                    .build()
            }
        }
        return null
    }
}
