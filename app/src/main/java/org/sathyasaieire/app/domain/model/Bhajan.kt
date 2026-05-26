package org.sathyasaieire.app.domain.model

data class Bhajan(
    val id: String = "",
    val title: String = "",
    val language: String = "English",
    val category: String = "",
    val lyrics: String = "",
    val audioUrl: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
)
