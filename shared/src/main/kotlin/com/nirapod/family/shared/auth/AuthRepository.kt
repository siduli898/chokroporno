package com.nirapod.family.shared.auth

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.nirapod.family.shared.models.User
import com.nirapod.family.shared.util.Constants
import kotlinx.coroutines.tasks.await

class AuthRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    val currentUser: FirebaseUser?
        get() = auth.currentUser

    suspend fun login(email: String, pass: String): Result<User> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, pass).await()
            val uid = result.user?.uid ?: throw Exception("ইউজার পাওয়া যায়নি")
            val doc = firestore.collection(Constants.FIRESTORE_USERS).document(uid).get().await()
            val user = doc.toObject(User::class.java) ?: User(id = uid, email = email)
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun register(name: String, email: String, pass: String, licenseKey: String): Result<User> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, pass).await()
            val uid = result.user?.uid ?: throw Exception("ইউজার তৈরিতে সমস্যা হয়েছে")
            val newUser = User(
                id = uid,
                name = name,
                email = email,
                role = "PARENT",
                licenseKey = licenseKey
            )
            firestore.collection(Constants.FIRESTORE_USERS).document(uid).set(newUser).await()
            Result.success(newUser)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun logout() {
        auth.signOut()
    }
}
