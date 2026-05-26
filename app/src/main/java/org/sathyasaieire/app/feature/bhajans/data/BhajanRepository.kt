package org.sathyasaieire.app.feature.bhajans.data

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import org.sathyasaieire.app.domain.model.Bhajan
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BhajanRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
) {
    private val col = firestore.collection("bhajans")

    fun getBhajans(): Flow<List<Bhajan>> = callbackFlow {
        val reg = col
            .orderBy("title", Query.Direction.ASCENDING)
            .addSnapshotListener { snap, err ->
                if (err != null) { close(err); return@addSnapshotListener }
                trySend(snap?.documents?.mapNotNull { it.toBhajan() } ?: emptyList())
            }
        awaitClose { reg.remove() }
    }

    suspend fun getById(id: String): Bhajan? = runCatching {
        col.document(id).get().await().toBhajan()
    }.getOrNull()

    private fun DocumentSnapshot.toBhajan(): Bhajan? {
        val title = getString("title") ?: return null
        return Bhajan(
            id = id,
            title = title,
            language = getString("language") ?: "English",
            category = getString("category") ?: "",
            lyrics = getString("lyrics") ?: "",
            audioUrl = getString("audioUrl"),
            createdAt = getLong("createdAt") ?: 0L,
        )
    }
}
