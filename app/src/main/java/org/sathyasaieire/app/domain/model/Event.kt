package org.sathyasaieire.app.domain.model

import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

data class Event(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val dateTimeMs: Long = 0L,
    val timezone: String = "Europe/Dublin",
    val location: String = "",
    val mapsLink: String? = null,
    val coverImageUrl: String? = null,
    val category: EventCategory = EventCategory.OTHER,
    val recurrence: RecurrenceType = RecurrenceType.NONE,
    val createdBy: String = "",
    val createdAt: Long = System.currentTimeMillis(),
) {
    fun toZonedDateTime(): ZonedDateTime =
        Instant.ofEpochMilli(dateTimeMs).atZone(ZoneId.of(timezone))

    val isUpcoming: Boolean get() = dateTimeMs > System.currentTimeMillis()

    fun formatDate(): String =
        toZonedDateTime().format(DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy"))

    fun formatTime(): String =
        toZonedDateTime().format(DateTimeFormatter.ofPattern("h:mm a z"))
}

enum class EventCategory(val displayName: String) {
    BHAJAN("Bhajan"),
    SATSANG("Satsang"),
    SEVA("Seva"),
    STUDY_CIRCLE("Study Circle"),
    FESTIVAL("Festival"),
    OTHER("Other");

    companion object {
        fun fromString(value: String): EventCategory =
            entries.firstOrNull { it.name.equals(value, ignoreCase = true) } ?: OTHER
    }
}

enum class RecurrenceType(val displayName: String) {
    NONE("Does not repeat"),
    WEEKLY("Repeats weekly"),
    MONTHLY("Repeats monthly");

    companion object {
        fun fromString(value: String): RecurrenceType =
            entries.firstOrNull { it.name.equals(value, ignoreCase = true) } ?: NONE
    }
}
