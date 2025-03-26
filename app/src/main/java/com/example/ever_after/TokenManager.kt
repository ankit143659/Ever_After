package com.example.ever_after

import android.content.Context
import android.util.Log
import com.google.auth.oauth2.GoogleCredentials
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException

object TokenManager {
    private var cachedToken: String? = null
    private var tokenExpiryTime: Long = 0L

    suspend fun getAccessToken(context: Context): String? {
        val currentTime = System.currentTimeMillis()

        // ✅ Pehle se token hai aur expire nahi hua, to wahi return karo
        if (cachedToken != null && currentTime < tokenExpiryTime) {
            Log.d("FCM", "Using Cached Token: $cachedToken")
            return cachedToken
        }

        return withContext(Dispatchers.IO) {
            try {
                val jsonStream = context.assets.open("service-account.json")
                val googleCredentials = GoogleCredentials.fromStream(jsonStream)
                    .createScoped(listOf("https://www.googleapis.com/auth/firebase.messaging"))

                googleCredentials.refreshIfExpired()
                val newToken = googleCredentials.accessToken?.tokenValue
                val expiresIn =
                    googleCredentials.accessToken?.expirationTime?.time ?: 0L // ✅ Fixed

                if (newToken != null) {
                    cachedToken = newToken
                    tokenExpiryTime = expiresIn
                }

                Log.d("FCM", "Generated New Token: $newToken")
                newToken
            } catch (e: IOException) {
                Log.e("FCM", "Error getting access token: ${e.message}")
                null
            }
        }
    }
}