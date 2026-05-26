package org.sathyasaieire.app.feature.whatsapp.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import org.sathyasaieire.app.domain.model.JoinRequest
import org.sathyasaieire.app.domain.model.JoinRequestStatus
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class JoinRequestRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
) {
    private val col = firestore.collection("joinRequests")

    suspend fun getMyRequest(): Result<JoinRequest?> = runCatching {
        val uid = auth.currentUser?.uid ?: return@runCatching null
        val doc = col.document(uid).get().await()
        if (doc.exists()) doc.toJoinRequest() else null
    }

    suspend fun submit(
        name: String,
        phone: String,
        county: String,
        message: String,
    ): Result<Unit> = runCatching {
        val uid = auth.currentUser?.uid ?: error("Not signed in")
        val email = auth.currentUser?.email ?: ""
        col.document(uid).set(
            mapOf(
                "userId" to uid,
                "name" to name,
                "email" to email,
                "phone" to phone,
                "county" to county,
                "message" to message,
                "status" to JoinRequestStatus.PENDING.name,
                "whatsappLink" to null,
                "createdAt" to System.currentTimeMillis(),
            )
        ).await()
    }

    private fun DocumentSnapshot.toJoinRequest() = JoinRequest(
        userId = getString("userId") ?: "",
        name = getString("name") ?: "",
        email = getString("email") ?: "",
        phone = getString("phone") ?: "",
        county = getString("county") ?: "",
        message = getString("message") ?: "",
        status = JoinRequestStatus.entries.firstOrNull {
            it.name == getString("status")
        } ?: JoinRequestStatus.PENDING,
        whatsappLink = getString("whatsappLink"),
        createdAt = getLong("createdAt") ?: 0L,
    )
}
