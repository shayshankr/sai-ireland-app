package org.sathyasaieire.app.feature.polls.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import org.sathyasaieire.app.domain.model.Poll
import org.sathyasaieire.app.domain.model.PollWithResults
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PollRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
) {
    private val col = firestore.collection("polls")

    fun getPolls(): Flow<List<Poll>> = callbackFlow {
        val reg = col
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snap, err ->
                if (err != null) { close(err); return@addSnapshotListener }
                trySend(snap?.documents?.mapNotNull { it.toPoll() } ?: emptyList())
            }
        awaitClose { reg.remove() }
    }

    suspend fun getPollWithResults(pollId: String): Result<PollWithResults> = runCatching {
        val uid = auth.currentUser?.uid
        val pollDoc = col.document(pollId).get().await()
        val poll = pollDoc.toPoll() ?: error("Poll not found")

        val voteDocs = col.document(pollId).collection("votes").get().await()
        val counts = MutableList(poll.options.size) { 0 }
        var myVoteIndex: Int? = null

        voteDocs.documents.forEach { doc ->
            val idx = doc.getLong("optionIndex")?.toInt() ?: return@forEach
            if (idx in counts.indices) counts[idx]++
            if (doc.id == uid) myVoteIndex = idx
        }

        PollWithResults(
            poll = poll,
            myVoteIndex = myVoteIndex,
            voteCounts = counts,
            totalVotes = voteDocs.size(),
        )
    }

    suspend fun vote(pollId: String, optionIndex: Int): Result<Unit> = runCatching {
        val uid = auth.currentUser?.uid ?: error("Not signed in")
        col.document(pollId).collection("votes").document(uid).set(
            mapOf(
                "optionIndex" to optionIndex,
                "userId" to uid,
                "createdAt" to System.currentTimeMillis(),
            )
        ).await()
    }

    suspend fun closePoll(pollId: String): Result<Unit> = runCatching {
        col.document(pollId).update(
            mapOf("isOpen" to false, "closedAt" to System.currentTimeMillis())
        ).await()
    }

    suspend fun createPoll(question: String, options: List<String>): Result<String> = runCatching {
        val uid = auth.currentUser?.uid ?: error("Not signed in")
        val ref = col.add(
            mapOf(
                "question" to question,
                "options" to options,
                "isOpen" to true,
                "createdBy" to uid,
                "createdAt" to System.currentTimeMillis(),
                "closedAt" to null,
            )
        ).await()
        ref.id
    }

    private fun DocumentSnapshot.toPoll(): Poll? {
        val question = getString("question") ?: return null
        @Suppress("UNCHECKED_CAST")
        val options = (get("options") as? List<String>) ?: return null
        return Poll(
            id = id,
            question = question,
            options = options,
            isOpen = getBoolean("isOpen") ?: true,
            createdBy = getString("createdBy") ?: "",
            createdAt = getLong("createdAt") ?: 0L,
            closedAt = getLong("closedAt"),
        )
    }
}
