package org.sathyasaieire.app.feature.contact.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ContactRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
) {
    suspend fun sendMessage(subject: String, message: String): Result<Unit> = runCatching {
        val user = auth.currentUser ?: error("Not signed in")
        firestore.collection("messages").add(
            mapOf(
                "userId" to user.uid,
                "name" to (user.displayName ?: ""),
                "email" to (user.email ?: ""),
                "subject" to subject,
                "message" to message,
                "createdAt" to System.currentTimeMillis(),
            )
        ).await()
    }
}
