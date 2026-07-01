package org.sathyasaieire.app.feature.contact.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import org.sathyasaieire.app.domain.model.ContactMessage
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ContactRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
) {
    private val col = firestore.collection("messages")

    suspend fun sendMessage(subject: String, message: String): Result<Unit> = runCatching {
        val user = auth.currentUser ?: error("Not signed in")
        col.add(
            mapOf(
                "userId" to user.uid,
                "name" to (user.displayName ?: ""),
                "email" to (user.email ?: ""),
                "subject" to subject,
                "message" to message,
                "isRead" to false,
                "createdAt" to System.currentTimeMillis(),
            )
        ).await()
    }

    // Used by admin inbox
    fun getAll(): Flow<List<ContactMessage>> = callbackFlow {
        val reg = col
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snap, err ->
                if (err != null) { close(err); return@addSnapshotListener }
                trySend(snap?.documents?.mapNotNull { doc ->
                    ContactMessage(
                        id = doc.id,
                        userId = doc.getString("userId") ?: "",
                        name = doc.getString("name") ?: "",
                        email = doc.getString("email") ?: "",
                        subject = doc.getString("subject") ?: "",
                        message = doc.getString("message") ?: "",
                        isRead = doc.getBoolean("isRead") ?: false,
                        createdAt = doc.getLong("createdAt") ?: 0L,
                    )
                } ?: emptyList())
            }
        awaitClose { reg.remove() }
    }

    suspend fun markRead(id: String, read: Boolean) {
        col.document(id).update("isRead", read).await()
    }

    suspend fun delete(id: String) {
        col.document(id).delete().await()
    }
}
