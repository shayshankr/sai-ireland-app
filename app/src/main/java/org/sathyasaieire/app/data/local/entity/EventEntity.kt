package org.sathyasaieire.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import org.sathyasaieire.app.domain.model.Event
import org.sathyasaieire.app.domain.model.EventCategory
import org.sathyasaieire.app.domain.model.RecurrenceType

@Entity(tableName = "events")
data class EventEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val dateTimeMs: Long,
    val timezone: String,
    val location: String,
    val mapsLink: String?,
    val coverImageUrl: String?,
    val category: String,
    val recurrence: String,
    val createdBy: String,
    val createdAt: Long,
)

fun EventEntity.toDomain() = Event(
    id = id,
    title = title,
    description = description,
    dateTimeMs = dateTimeMs,
    timezone = timezone,
    location = location,
    mapsLink = mapsLink,
    coverImageUrl = coverImageUrl,
    category = EventCategory.fromString(category),
    recurrence = RecurrenceType.fromString(recurrence),
    createdBy = createdBy,
    createdAt = createdAt,
)

fun Event.toEntity() = EventEntity(
    id = id,
    title = title,
    description = description,
    dateTimeMs = dateTimeMs,
    timezone = timezone,
    location = location,
    mapsLink = mapsLink,
    coverImageUrl = coverImageUrl,
    category = category.name,
    recurrence = recurrence.name,
    createdBy = createdBy,
    createdAt = createdAt,
)
