package com.nirapod.family.shared.firebase

import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.tasks.await

object FCMHelper {
    suspend fun getDeviceFCMToken(): String? {
        return try {
            FirebaseMessaging.getInstance().token.await()
        } catch (_: Exception) {
            null
        }
    }
}
