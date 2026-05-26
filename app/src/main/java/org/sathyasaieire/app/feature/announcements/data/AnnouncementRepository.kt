package org.sathyasaieire.app.feature.announcements.data

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import org.sathyasaieire.app.domain.model.Announcement
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AnnouncementRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
) {
    private val col = firestore.collection("announcements")

    fun getAll(): Flow<List<Announcement>> = callbackFlow {
        val reg = col
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snap, err ->
                if (err != null) { close(err); return@addSnapshotListener }
                trySend(snap?.documents?.mapNotNull { doc ->
                    Announcement(
                        id = doc.id,
                        title = doc.getString("title") ?: return@mapNotNull null,
                        body = doc.getString("body") ?: "",
                        isActive = doc.getBoolean("isActive") ?: true,
                        createdAt = doc.getLong("createdAt") ?: 0L,
                    )
                } ?: emptyList())
            }
        awaitClose { reg.remove() }
    }

    suspend fun create(title: String, body: String) {
        col.add(
            mapOf(
                "title" to title,
                "body" to body,
                "isActive" to true,
                "createdAt" to System.currentTimeMillis(),
            )
        ).await()
    }

    suspend fun update(id: String, title: String, body: String) {
        col.document(id).update(
            mapOf("title" to title, "body" to body)
        ).await()
    }

    suspend fun setActive(id: String, active: Boolean) {
        col.document(id).update("isActive", active).await()
    }

    suspend fun delete(id: String) {
        col.document(id).delete().await()
    }

    suspend fun getById(id: String): Announcement? = runCatching {
        col.document(id).get().await().let { doc ->
            Announcement(
                id = doc.id,
                title = doc.getString("title") ?: return@let null,
                body = doc.getString("body") ?: "",
                isActive = doc.getBoolean("isActive") ?: true,
                createdAt = doc.getLong("createdAt") ?: 0L,
            )
        }
    }.getOrNull()
}
