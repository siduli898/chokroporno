package com.nirapod.family.shared.firebase

import com.google.firebase.firestore.FirebaseFirestore
import com.nirapod.family.shared.models.License
import com.nirapod.family.shared.util.Constants
import kotlinx.coroutines.tasks.await

class LicenseRepository(private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()) {

    suspend fun validateAndBindLicense(key: String, deviceId: String): Result<License> {
        return try {
            val docRef = firestore.collection(Constants.FIRESTORE_LICENSES).document(key)
            val snapshot = docRef.get().await()

            if (!snapshot.exists()) {
                return Result.failure(Exception("অবৈধ লাইসেন্স কী (Invalid License Key)"))
            }

            val license = snapshot.toObject(License::class.java)
                ?: return Result.failure(Exception("লাইসেন্স ডেটা পাওয়া যায়নি"))

            if (!license.isActive) {
                return Result.failure(Exception("লাইসেন্সটি সক্রিয় নয় (Inactive License)"))
            }

            if (license.expiry > 0 && license.expiry < System.currentTimeMillis()) {
                return Result.failure(Exception("লাইসেন্সের মেয়াদ শেষ হয়ে গেছে (License Expired)"))
            }

            val boundList = license.boundDeviceIds.toMutableList()
            if (!boundList.contains(deviceId)) {
                if (boundList.size >= license.deviceLimit) {
                    return Result.failure(Exception("ডিভাইস লিমিট (${license.deviceLimit}) পূর্ণ হয়ে গেছে!"))
                }
                boundList.add(deviceId)
                docRef.update("boundDeviceIds", boundList).await()
            }

            Result.success(license.copy(boundDeviceIds = boundList))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getLicense(key: String): Result<License> {
        return try {
            val docRef = firestore.collection(Constants.FIRESTORE_LICENSES).document(key)
            val snapshot = docRef.get().await()
            val license = snapshot.toObject(License::class.java)
            if (license != null) Result.success(license) else Result.failure(Exception("Not found"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
