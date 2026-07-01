package org.sathyasaieire.app.domain.model

data class ContactMessage(
    val id: String = "",
    val userId: String = "",
    val name: String = "",
    val email: String = "",
    val subject: String = "",
    val message: String = "",
    val isRead: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
)
