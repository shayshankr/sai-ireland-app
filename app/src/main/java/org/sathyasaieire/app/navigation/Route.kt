package org.sathyasaieire.app.navigation

sealed class Route(val path: String) {
    data object SignIn : Route("sign_in")
    data object GdprConsent : Route("gdpr_consent")
    data object Home : Route("home")
    data object Events : Route("events")
    data object EventDetail : Route("event_detail/{eventId}") {
        fun createRoute(eventId: String) = "event_detail/$eventId"
    }
    data object Polls : Route("polls")
    data object PollDetail : Route("poll_detail/{pollId}") {
        fun createRoute(pollId: String) = "poll_detail/$pollId"
    }
    data object News : Route("news")
    data object More : Route("more")
    data object WhatsApp : Route("whatsapp_join")
    data object Contact : Route("contact")
    data object Gallery : Route("gallery")
    data object Bhajans : Route("bhajans")
    data object BhajanDetail : Route("bhajan_detail/{bhajanId}") {
        fun createRoute(bhajanId: String) = "bhajan_detail/$bhajanId"
    }
    data object Seva : Route("seva")
    data object Timer : Route("timer")
    data object Search : Route("search")
    data object Settings : Route("settings")
    data object Profile : Route("profile")
    data object Admin : Route("admin")
    data object AdminPollCreate : Route("admin_poll_create")
    data object AdminEventCreate : Route("admin_event_create")
    data object AdminEventEdit : Route("admin_event_edit/{eventId}") {
        fun createRoute(eventId: String) = "admin_event_edit/$eventId"
    }
    data object AdminAnnouncements : Route("admin_announcements")
    data object AdminAnnouncementCreate : Route("admin_announcement_create")
    data object AdminAnnouncementEdit : Route("admin_announcement_edit/{announcementId}") {
        fun createRoute(announcementId: String) = "admin_announcement_edit/$announcementId"
    }
    data object AdminThoughtOfDay : Route("admin_thought_of_day")
    data object AdminInbox : Route("admin_inbox")
    data object PrivacyPolicy : Route("privacy_policy")
}
