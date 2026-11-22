package com.nirapod.family.shared.models

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class User(
    @DocumentId val id: String = "",
    val email: String = "",
    val name: String = "",
    val role: String = "PARENT", // PARENT, CHILD, ADMIN
    val licenseKey: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

data class Device(
    @DocumentId val deviceId: String = "",
    val parentId: String = "",
    val licenseKey: String = "",
    val deviceName: String = "",
    val model: String = "",
    val osVersion: String = "",
    val batteryLevel: Int = 100,
    val isCharging: Boolean = false,
    val isOnline: Boolean = false,
    val isHidden: Boolean = false,
    val lastHeartbeat: Long = System.currentTimeMillis(),
    val fcmToken: String = "",
    val currentStreamType: String? = null // "screen", "camera", "audio", null
)

data class License(
    @DocumentId val key: String = "",
    val tier: String = "STANDARD", // BASIC, STANDARD, PREMIUM
    val deviceLimit: Int = 3,
    val expiry: Long = 0L,
    val boundDeviceIds: List<String> = emptyList(),
    val ownerEmail: String = "",
    val isActive: Boolean = true
)

data class LocationPoint(
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val accuracy: Float = 0f,
    val speed: Float = 0f,
    val timestamp: Long = System.currentTimeMillis(),
    val address: String = ""
)

data class StreamCommand(
    val command: String = "", // "start_screen", "start_camera", "start_audio", "stop_all", "get_location", "hide_app", "unhide_app"
    val targetDeviceId: String = "",
    val senderId: String = "",
    val cameraFacing: String = "front", // "front" or "back"
    val timestamp: Long = System.currentTimeMillis()
)

data class SignalingMessage(
    val type: String = "", // "offer", "answer", "ice_candidate", "bye"
    val sdp: String? = null,
    val sdpMid: String? = null,
    val sdpMLineIndex: Int = 0,
    val candidate: String? = null,
    val senderDeviceId: String = "",
    val targetDeviceId: String = "",
    val streamType: String = "screen" // "screen", "camera", "audio"
)
