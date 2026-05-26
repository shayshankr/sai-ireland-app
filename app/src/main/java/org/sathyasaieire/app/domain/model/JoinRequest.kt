package org.sathyasaieire.app.domain.model

data class JoinRequest(
    val userId: String = "",
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val county: String = "",
    val message: String = "",
    val status: JoinRequestStatus = JoinRequestStatus.PENDING,
    val whatsappLink: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
)

enum class JoinRequestStatus(val label: String) {
    PENDING("Pending review"),
    APPROVED("Approved"),
    REJECTED("Not approved"),
}
