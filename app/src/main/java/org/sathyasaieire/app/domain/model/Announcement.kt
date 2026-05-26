package org.sathyasaieire.app.domain.model

data class Announcement(
    val id: String = "",
    val title: String = "",
    val body: String = "",
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
)
