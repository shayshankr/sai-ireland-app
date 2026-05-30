package org.sathyasaieire.app.feature.home.data

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import org.sathyasaieire.app.domain.model.Announcement
import org.sathyasaieire.app.domain.model.Quote
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

private data class FallbackThought(val text: String, val source: String = "Sanathana Sarathi")

private val FALLBACK_QUOTES = listOf(
    FallbackThought("Love all, serve all. Help ever, hurt never."),
    FallbackThought("The end of education is character."),
    FallbackThought("Start the day with love, fill the day with love, end the day with love — that is the way to God."),
    FallbackThought("You are not one person but three: the one you think you are, the one others think you are, and the one you really are."),
    FallbackThought("Life is a song — sing it. Life is a game — play it. Life is a challenge — meet it. Life is a dream — realise it."),
    FallbackThought("There is only one religion, the religion of love."),
    FallbackThought("The greatest gift you can give is your love. It costs nothing and means everything."),
    FallbackThought("Be like a lotus. Let the beauty of your heart speak."),
    FallbackThought("Do not be led by others; be led by your own conscience."),
    FallbackThought("Service to man is service to God."),
    FallbackThought("God is not an object to be worshipped outside. God is the love that dwells within you."),
    FallbackThought("Whatever you do, offer it to God. That is the highest form of prayer."),
    FallbackThought("Forbearance is the greatest virtue. Compassion is the greatest strength."),
    FallbackThought("Joy and peace are already within you. Turn inward and find them."),
)

@Singleton
class HomeRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
) {
    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    suspend fun getTodayQuote(): Quote {
        val today = LocalDate.now().format(dateFormatter)
        return try {
            val doc = firestore.collection("quotes").document(today).get().await()
            if (doc.exists()) {
                Quote(
                    text = doc.getString("text") ?: fallbackThought(today).text,
                    attribution = doc.getString("attribution") ?: "Sri Sathya Sai Baba",
                    source = doc.getString("source") ?: "",
                    date = today,
                )
            } else {
                fallbackThought(today).let { Quote(text = it.text, source = it.source, date = today) }
            }
        } catch (e: Exception) {
            fallbackThought(today).let { Quote(text = it.text, source = it.source, date = today) }
        }
    }

    suspend fun getThoughtForDate(date: String): Quote? = try {
        val doc = firestore.collection("quotes").document(date).get().await()
        if (doc.exists()) Quote(
            text = doc.getString("text") ?: "",
            attribution = doc.getString("attribution") ?: "Sri Sathya Sai Baba",
            source = doc.getString("source") ?: "",
            date = date,
        ) else null
    } catch (e: Exception) {
        null
    }

    suspend fun saveThought(date: String, text: String, attribution: String, source: String): Result<Unit> = try {
        firestore.collection("quotes").document(date).set(
            mapOf(
                "text" to text,
                "attribution" to attribution,
                "source" to source,
                "updatedAt" to System.currentTimeMillis(),
            )
        ).await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun getActiveAnnouncements(): List<Announcement> {
        return try {
            firestore.collection("announcements")
                .whereEqualTo("isActive", true)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(3)
                .get().await()
                .documents.mapNotNull { doc ->
                    Announcement(
                        id = doc.id,
                        title = doc.getString("title") ?: return@mapNotNull null,
                        body = doc.getString("body") ?: "",
                        isActive = doc.getBoolean("isActive") ?: true,
                        createdAt = doc.getLong("createdAt") ?: 0L,
                    )
                }
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun fallbackThought(dateStr: String): FallbackThought {
        val day = try { LocalDate.parse(dateStr, dateFormatter).dayOfYear } catch (e: Exception) { 0 }
        return FALLBACK_QUOTES[day % FALLBACK_QUOTES.size]
    }
}
