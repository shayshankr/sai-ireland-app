package org.sathyasaieire.app.feature.events.data

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import org.sathyasaieire.app.data.local.dao.EventDao
import org.sathyasaieire.app.data.local.entity.toDomain
import org.sathyasaieire.app.data.local.entity.toEntity
import org.sathyasaieire.app.domain.model.Event
import org.sathyasaieire.app.domain.model.EventCategory
import org.sathyasaieire.app.domain.model.RecurrenceType
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EventRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val eventDao: EventDao,
) {
    fun getUpcoming(): Flow<List<Event>> = eventDao.getUpcoming().map { it.map(::toDomainMapper) }
    fun getPast(): Flow<List<Event>> = eventDao.getPast().map { it.map(::toDomainMapper) }
    fun getAll(): Flow<List<Event>> = eventDao.getAll().map { it.map(::toDomainMapper) }

    suspend fun getById(id: String): Event? = eventDao.getById(id)?.toDomain()

    suspend fun refresh(): Result<Unit> = try {
        val docs = firestore.collection("events")
            .orderBy("dateTimeMs", Query.Direction.ASCENDING)
            .get().await()
        val events = docs.documents.mapNotNull { it.toEvent() }
        eventDao.deleteAll()
        eventDao.upsertAll(events.map { it.toEntity() })
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    // Used by admin (Feature 3)
    suspend fun save(event: Event): Result<Unit> = try {
        val data = mapOf(
            "title" to event.title,
            "description" to event.description,
            "dateTimeMs" to event.dateTimeMs,
            "timezone" to event.timezone,
            "location" to event.location,
            "mapsLink" to event.mapsLink,
            "coverImageUrl" to event.coverImageUrl,
            "category" to event.category.name,
            "recurrence" to event.recurrence.name,
            "createdBy" to event.createdBy,
            "createdAt" to event.createdAt,
        )
        if (event.id.isEmpty()) {
            firestore.collection("events").add(data).await()
        } else {
            firestore.collection("events").document(event.id).set(data).await()
        }
        eventDao.upsert(event.toEntity())
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun delete(id: String): Result<Unit> = try {
        firestore.collection("events").document(id).delete().await()
        eventDao.deleteById(id)
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }
}

private fun toDomainMapper(entity: org.sathyasaieire.app.data.local.entity.EventEntity) = entity.toDomain()

private fun com.google.firebase.firestore.DocumentSnapshot.toEvent(): Event? = try {
    Event(
        id = id,
        title = getString("title") ?: return null,
        description = getString("description") ?: "",
        dateTimeMs = getLong("dateTimeMs") ?: return null,
        timezone = getString("timezone") ?: "Europe/Dublin",
        location = getString("location") ?: "",
        mapsLink = getString("mapsLink"),
        coverImageUrl = getString("coverImageUrl"),
        category = EventCategory.fromString(getString("category") ?: ""),
        recurrence = RecurrenceType.fromString(getString("recurrence") ?: ""),
        createdBy = getString("createdBy") ?: "",
        createdAt = getLong("createdAt") ?: 0L,
    )
} catch (e: Exception) {
    null
}
