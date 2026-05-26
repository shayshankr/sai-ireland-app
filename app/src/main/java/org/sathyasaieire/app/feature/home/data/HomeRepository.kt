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

private val FALLBACK_QUOTES = listOf(
    "Love all, serve all. Help ever, hurt never.",
    "The end of education is character.",
    "Start the day with love, fill the day with love, end the day with love — that is the way to God.",
    "You are not one person but three: the one you think you are, the one others think you are, and the one you really are.",
    "Life is a song — sing it. Life is a game — play it. Life is a challenge — meet it. Life is a dream — realise it.",
    "There is only one religion, the religion of love.",
    "The greatest gift you can give is your love. It costs nothing and means everything.",
    "Be like a lotus. Let the beauty of your heart speak.",
    "Do not be led by others; be led by your own conscience.",
    "Service to man is service to God.",
    "God is not an object to be worshipped outside. God is the love that dwells within you.",
    "Whatever you do, offer it to God. That is the highest form of prayer.",
    "Forbearance is the greatest virtue. Compassion is the greatest strength.",
    "Joy and peace are already within you. Turn inward and find them.",
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
                    text = doc.getString("text") ?: fallbackQuote(today),
                    attribution = doc.getString("attribution") ?: "Sri Sathya Sai Baba",
                    date = today,
                )
            } else {
                Quote(text = fallbackQuote(today), date = today)
            }
        } catch (e: Exception) {
            Quote(text = fallbackQuote(today), date = today)
        }
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

    private fun fallbackQuote(dateStr: String): String {
        val day = try { LocalDate.parse(dateStr, dateFormatter).dayOfYear } catch (e: Exception) { 0 }
        return FALLBACK_QUOTES[day % FALLBACK_QUOTES.size]
    }
}
