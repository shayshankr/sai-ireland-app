package org.sathyasaieire.app.domain.model

data class User(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val photoUrl: String? = null,
    val phone: String? = null,
    val role: UserRole = UserRole.MEMBER,
    val gdprConsent: Boolean = false,
    val analyticsEnabled: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
)

enum class UserRole {
    MEMBER, ADMIN, SUPER_ADMIN;

    companion object {
        fun fromString(value: String): UserRole = when (value.lowercase()) {
            "admin" -> ADMIN
            "super_admin" -> SUPER_ADMIN
            else -> MEMBER
        }
    }
}
