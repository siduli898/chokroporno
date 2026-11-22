package com.nirapod.family.shared.firebase

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.nirapod.family.shared.models.Device
import com.nirapod.family.shared.models.LocationPoint
import com.nirapod.family.shared.models.StreamCommand
import com.nirapod.family.shared.util.Constants
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirebaseRepository(private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()) {

    suspend fun registerOrUpdateDevice(device: Device): Result<Unit> {
        return try {
            firestore.collection(Constants.FIRESTORE_DEVICES)
                .document(device.deviceId)
                .set(device)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateDeviceHeartbeat(deviceId: String, battery: Int, isCharging: Boolean, isOnline: Boolean) {
        try {
            firestore.collection(Constants.FIRESTORE_DEVICES).document(deviceId).update(
                mapOf(
                    "batteryLevel" to battery,
                    "isCharging" to isCharging,
                    "isOnline" to isOnline,
                    "lastHeartbeat" to System.currentTimeMillis()
                )
            ).await()
        } catch (_: Exception) {}
    }

    suspend fun updateDeviceStreamStatus(deviceId: String, streamType: String?) {
        try {
            firestore.collection(Constants.FIRESTORE_DEVICES).document(deviceId).update(
                "currentStreamType", streamType
            ).await()
        } catch (_: Exception) {}
    }

    suspend fun saveLocation(deviceId: String, point: LocationPoint) {
        try {
            firestore.collection(Constants.FIRESTORE_DEVICES)
                .document(deviceId)
                .collection(Constants.FIRESTORE_LOCATIONS)
                .add(point)
                .await()
        } catch (_: Exception) {}
    }

    fun observeDevicesForParent(licenseKey: String): Flow<List<Device>> = callbackFlow {
        val listener: ListenerRegistration = firestore.collection(Constants.FIRESTORE_DEVICES)
            .whereEqualTo("licenseKey", licenseKey)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val list = snapshot?.documents?.mapNotNull { it.toObject(Device::class.java) } ?: emptyList()
                trySend(list)
            }
        awaitClose { listener.remove() }
    }

    fun observeDevice(deviceId: String): Flow<Device?> = callbackFlow {
        val listener = firestore.collection(Constants.FIRESTORE_DEVICES)
            .document(deviceId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                trySend(snapshot?.toObject(Device::class.java))
            }
        awaitClose { listener.remove() }
    }

    fun observeLatestLocations(deviceId: String, limit: Long = 10): Flow<List<LocationPoint>> = callbackFlow {
        val listener = firestore.collection(Constants.FIRESTORE_DEVICES)
            .document(deviceId)
            .collection(Constants.FIRESTORE_LOCATIONS)
            .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .limit(limit)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val list = snapshot?.documents?.mapNotNull { it.toObject(LocationPoint::class.java) } ?: emptyList()
                trySend(list)
            }
        awaitClose { listener.remove() }
    }

    suspend fun sendCommand(command: StreamCommand): Result<Unit> {
        return try {
            firestore.collection("commands").add(command).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
